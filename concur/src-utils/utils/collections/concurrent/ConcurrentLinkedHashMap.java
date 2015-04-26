/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.collections.concurrent;

import utils.collections.concurrent.ConcurrentLinkedHashMap.Node.State;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

// 支持FIFO,LRU,SECOND_CHANCE

/**
 * A {@link ConcurrentMap} with a doubly-linked list running through its entries.
 *
 * This class provides the same semantics as a {@link ConcurrentHashMap} in terms of
 * iterators, acceptable keys, and concurrency characteristics, but perform slightly
 * worse due to the added expense of maintaining the linked list. It differs from
 * {@link java.util.LinkedHashMap} in that it does not provide predictable iteration
 * order.
 *
 * This map is intended to be used for caches and provides the following eviction policies:
 * <ul>
 * <li> First-in, First-out: Also known as insertion order. This policy has excellent
 * concurrency characteristics and an adequate hit rate.
 * <li> Second-chance: An enhanced FIFO policy that marks entries that have been retrieved
 * and saves them from being evicted until the next pass. This enhances the FIFO policy
 * by making it aware of "hot" entries, which increases its hit rate to be equal to an
 * LRU's under normal workloads. In the worst case, where all entries have been saved,
 * this policy degrades to a FIFO.
 * <li> Least Recently Used: An eviction policy based on the observation that entries that
 * have been used recently will likely be used again soon. This policy provides a good
 * approximation of an optimal algorithm, but suffers by being expensive to maintain.
 * The cost of reordering entries on the list during every access operation reduces
 * the concurrency and performance characteristics of this policy.
 * </ul>
 *
 * The <i>Second Chance</i> eviction policy is recommended for common use cases as it provides
 * the best mix of performance and efficiency of the supported replacement policies.
 *
 * If the <i>Least Recently Used</i> policy is chosen then the sizing should compensate for the
 * proliferation of dead nodes on the linked list. While the values are removed immediately, the
 * nodes are evicted only when they reach the head of the list. Under FIFO-based policies, dead
 * nodes occur when explicit removals are requested and does not normally produce a noticeable
 * impact on the map's hit rate. The LRU policy creates a dead node on every successful retrieval
 * and a new node is placed at the tail of the list. For this reason, the LRU's efficiency cannot
 * be compared directly to a {@link java.util.LinkedHashMap} evicting in access order.
 *
 * @author <a href="mailto:ben.manes@reardencommerce.com">Ben Manes</a>
 */
public class ConcurrentLinkedHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {
    private static final long serialVersionUID = 8350170357874293408L;
    final List<EvictionListener<K, V>> listeners;
    final ConcurrentMap<K, Node<K, V>> data;
    final AtomicInteger capacity;
    final EvictionPolicy policy;
    final AtomicInteger length;
    final Node<K, V> head;
    final Node<K, V> tail;

    /**
     * Creates a new, empty, unbounded map with the specified maximum capacity and the default
     * concurrencyLevel.
     *
     * @param policy          The eviction policy to apply when the size exceeds the maximum capacity.
     * @param maximumCapacity The maximum capacity to coerces to. The size may exceed it temporarily.
     * @param listeners       The listeners registered for notification when an entry is evicted.
     */
    public ConcurrentLinkedHashMap(EvictionPolicy policy, int maximumCapacity, EvictionListener<K, V>... listeners) {
        this(policy, maximumCapacity, 16, listeners);
    }

    /**
     * Creates a new, empty, unbounded map with the specified maximum capacity and concurrency level.
     *
     * @param policy           The eviction policy to apply when the size exceeds the maximum capacity.
     * @param maximumCapacity  The maximum capacity to coerces to. The size may exceed it temporarily.
     * @param concurrencyLevel The estimated number of concurrently updating threads. The implementation
     *                         performs internal sizing to try to accommodate this many threads.
     * @param listeners        The listeners registered for notification when an entry is evicted.
     */
    public ConcurrentLinkedHashMap(EvictionPolicy policy, int maximumCapacity, int concurrencyLevel, EvictionListener<K, V>... listeners) {
        if ((policy == null) || (maximumCapacity < 0) || (concurrencyLevel <= 0)) {
            throw new IllegalArgumentException();
        }
        this.listeners = (listeners == null) ? Collections.<EvictionListener<K, V>>emptyList() : Arrays.asList(listeners);
        this.data = new ConcurrentHashMap<K, Node<K, V>>(100, 0.75f, concurrencyLevel);
        this.capacity = new AtomicInteger(maximumCapacity);
        this.length = new AtomicInteger();
        this.head = new Node<K, V>();
        this.tail = new Node<K, V>();
        this.policy = policy;

        head.setPrev(head);
        head.setNext(tail);
        tail.setPrev(head);
        tail.setNext(tail);
    }

    /**
     * Determines whether the map has exceeded its capacity.
     *
     * @return Whether the map has overflowed and an entry should be evicted.
     */
    private boolean isOverflow() {
        return length.get() > capacity();
    }

    /**
     * Sets the maximum capacity of the map and eagerly evicts entries until the
     * it shrinks to the appropriate size.
     *
     * @param capacity The maximum capacity of the map.
     */
    public void setCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        this.capacity.set(capacity);
        while (isOverflow()) {
            evict();
        }
    }

    /**
     * Retrieves the maximum capacity of the map.
     *
     * @return The maximum capacity.
     */
    public int capacity() {
        return capacity.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return data.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        for (K key : keySet()) {
            remove(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(new Node<Object, Object>(null, value));
    }

    /**
     * Evicts a single entry if the map exceeds the maximum capacity.
     */
    private void evict() {
        while (isOverflow()) {
            Node<K, V> node = poll();
            if (node == null) {
                return;
            } else if (policy.onEvict(this, node)) {
                V value = node.getValue();
                if (value != null) {
                    K key = node.getKey();
                    data.remove(key);
                    notifyEviction(key, value);
                }
                length.decrementAndGet();
                return;
            }
            offer(node);
        }
    }

    /**
     * Notifies the listeners that an entry was evicted from the map.
     *
     * @param key   The entry's key.
     * @param value The entry's value.
     */
    private void notifyEviction(K key, V value) {
        for (EvictionListener<K, V> listener : listeners) {
            listener.onEviction(key, value);
        }
    }


    /**
     * Retrieves and removes the first node on the list or <tt>null</tt> if empty.
     *
     * @return The first node on the list or <tt>null</tt> if empty.
     */
    private Node<K, V> poll() {
        for (; ;) {
            Node<K, V> node = head.getNext();
            if (head.casNext(node, node.getNext())) {
                for (; ;) {
                    if (node.casState(State.LINKED, State.UNLINKING)) {
                        node.getNext().setPrev(head);
                        node.setState(State.UNLINKED);
                        return node;
                    }
                    State state = node.getState();
                    if (state == State.SENTINEL) {
                        return null;
                    }
                    continue; // retry CAS as the node is being linked by offer
                }
            }
        }
    }

    /**
     * Inserts the specified node on to the tail of the list.
     *
     * @param node An unlinked node to append to the tail of the list.
     */
    private void offer(Node<K, V> node) {
        node.setState(State.LINKING);
        node.setNext(tail);
        for (; ;) {
            Node<K, V> prev = tail.getPrev();
            node.setPrev(prev);
            if (prev.casNext(tail, node)) {
                Node<K, V> next = tail;
                for (; ;) {
                    if (next.casPrev(prev, node)) {
                        node.setState(State.LINKED);
                        return;
                    }
                    // walk up the list until a node can be linked
                    next = next.getPrev();
                }
            }
        }
    }

    /**
     * Adds a node to the list and data store if it does not already exist.
     *
     * @param node An unlinked node to add.
     * @return The previous value in the data store.
     */
    private Node<K, V> putIfAbsent(Node<K, V> node) {
        Node<K, V> old = data.putIfAbsent(node.getKey(), node);
        if (old == null) {
            length.incrementAndGet();
            offer(node);
            evict();
        }
        return old;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Object key) {
        Node<K, V> node = data.get(key);
        if (node != null) {
            V value = node.getValue();
            policy.onGet(this, node);
            return value;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public V put(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        Node<K, V> old = putIfAbsent(new Node<K, V>(key, value));
        return (old == null) ? null : old.getAndSetValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public V putIfAbsent(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        Node<K, V> old = putIfAbsent(new Node<K, V>(key, value));
        return (old == null) ? null : old.getValue();
    }

    /**
     * {@inheritDoc}
     */
    public V remove(Object key) {
        Node<K, V> node = data.remove(key);
        if (node != null) {
            V value = node.getValue();
            policy.onRemove(this, node);
            return value;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(Object key, Object value) {
        Node<K, V> node = data.get(key);
        if ((node != null) && node.value.equals(value) && data.remove(key, node)) {
            policy.onRemove(this, node);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public V replace(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        Node<K, V> node = data.get(key);
        return (node == null) ? null : node.getAndSetValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public boolean replace(K key, V oldValue, V newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException();
        }
        Node<K, V> node = data.get(key);
        return (node == null) ? false : node.casValue(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Entry<K, V>> entrySet() {
        return new EntrySetAdapter();
    }

    /**
     * A listener registered for notification when an entry is evicted.
     */
    public interface EvictionListener<K, V> {

        /**
         * A call-back notification that the entry was evicted.
         *
         * @param key   The evicted key.
         * @param value The evicted value.
         */
        void onEviction(K key, V value);
    }

    /**
     * The replacement policy to apply to determine which entry to discard to when the capacity has been reached.
     */
    public enum EvictionPolicy {

        /**
         * Evicts entries based on insertion order.
         */
        FIFO() {
            <K, V> void onGet(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                // do nothing
            }
            <K, V> boolean onEvict(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                return true;
            }
        },

        /**
         * Evicts entries based on insertion order, but gives an entry a "second chance" if it has been requested recently.
         */
        SECOND_CHANCE() {
            <K, V> void onGet(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                node.setMarked(true);
            }
            <K, V> void onRemove(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                super.onRemove(map, node);
                node.setMarked(false);
            }
            <K, V> boolean onEvict(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                if (node.isMarked()) {
                    node.setMarked(false);
                    return false;
                }
                return true;
            }
        },


        /**
         * Evicts entries based on how recently they are used, with the least recent evicted first.
         */
        LRU() {
            <K, V> void onGet(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                Node<K, V> newNode = new Node<K, V>(node.getKey(), node.getValue());
                if (map.data.replace(node.getKey(), node, newNode)) {
                    map.length.incrementAndGet();
                    onRemove(map, node);
                    map.offer(newNode);
                    map.evict();
                }
            }
            <K, V> boolean onEvict(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
                return true;
            }
        };

        /**
         * Performs any operations required by the policy after a node was successfully retrieved.
         */
        abstract <K, V> void onGet(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node);

        /**
         * Expires a node so that, for all intents and purposes, it is a dead on the list. The
         * caller of this method should have already removed the node from the mapping so that
         * no key can look it up. When the node reaches the head of the list it will be evicted.
         */
        <K, V> void onRemove(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node) {
            node.setValue(null);
        }

        /**
         * Determines whether to evict the node at the head of the list. If false, the node is offered
         * to the tail.
         */
        abstract <K, V> boolean onEvict(ConcurrentLinkedHashMap<K, V> map, Node<K, V> node);
    }

    /**
     * A node on the double-linked list. This list cross-cuts the data store.
     */
    @SuppressWarnings("unchecked")
    static final class Node<K, V> implements Serializable {
        private static final long serialVersionUID = 1461281468985304519L;
        private static final AtomicReferenceFieldUpdater<Node, Object> valueUpdater =
                AtomicReferenceFieldUpdater.newUpdater(Node.class, Object.class, "value");
        private static final AtomicReferenceFieldUpdater<Node, State> stateUpdater =
                AtomicReferenceFieldUpdater.newUpdater(Node.class, State.class, "state");
        private static final AtomicReferenceFieldUpdater<Node, Node> prevUpdater =
                AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "prev");
        private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater =
                AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

        public static enum State {
            SENTINEL, UNLINKED, UNLINKING, LINKING, LINKED
        }

        private final K key;
        private volatile V value;
        private volatile State state;
        private volatile boolean marked;
        private volatile Node<K, V> prev;
        private volatile Node<K, V> next;

        /**
         * Creates a sentinel node.
         */
        public Node() {
            this.key = null;
            this.state = State.SENTINEL;
        }

        /**
         * Creates a new, unlinked node.
         */
        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.state = State.UNLINKED;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            valueUpdater.set(this, value);
        }

        public V getAndSetValue(V value) {
            return (V) valueUpdater.getAndSet(this, value);
        }

        public boolean casValue(V expect, V update) {
            return valueUpdater.compareAndSet(this, expect, update);
        }

        public Node<K, V> getPrev() {
            return prev;
        }

        public void setPrev(Node<K, V> node) {
            prevUpdater.set(this, node);
        }

        public boolean casPrev(Node<K, V> expect, Node<K, V> update) {
            return prevUpdater.compareAndSet(this, expect, update);
        }

        public Node<K, V> getNext() {
            return next;
        }

        public void setNext(Node<K, V> node) {
            nextUpdater.set(this, node);
        }

        public boolean casNext(Node<K, V> expect, Node<K, V> update) {
            return nextUpdater.compareAndSet(this, expect, update);
        }

        public boolean isMarked() {
            return marked;
        }

        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            stateUpdater.set(this, state);
        }

        public boolean casState(State expect, State update) {
            return stateUpdater.compareAndSet(this, expect, update);
        }

        /**
         * Only ensures that the values are equal, as the key may be <tt>null</tt> for look-ups.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof Node)) {
                return false;
            }
            V value = getValue();
            Node<?, ?> node = (Node<?, ?>) obj;
            return (value == null) ? (node.getValue() == null) : value.equals(node.getValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return ((key == null) ? 0 : key.hashCode()) ^
                    ((value == null) ? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return String.format("Node[state=%s, marked=%b, key=%s, value=%s]", getState(), isMarked(), getKey(), getValue());
        }
    }

    /**
     * An adapter to represent the data store's entry set in the external type.
     */
    private final class EntrySetAdapter extends AbstractSet<Entry<K, V>> {
        private final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

        /**
         * {@inheritDoc}
         */
        @Override
        public void clear() {
            map.clear();
        }

        /**
         * {@inheritDoc}
         */
        public int size() {
            return map.size();
        }

        /**
         * {@inheritDoc}
         */
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIteratorAdapter(map.data.entrySet().iterator());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            Node<K, V> node = map.data.get(entry.getKey());
            return (node != null) && (node.value.equals(entry.getValue()));
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add(Entry<K, V> entry) {
            return (map.putIfAbsent(entry.getKey(), entry.getValue()) == null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            return map.remove(entry.getKey(), entry.getValue());
        }
    }

    /**
     * An adapter to represent the data store's entry iterator in the external type.
     */
    private final class EntryIteratorAdapter implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, Node<K, V>>> iterator;
        private Entry<K, Node<K, V>> current;

        public EntryIteratorAdapter(Iterator<Entry<K, Node<K, V>>> iterator) {
            this.iterator = iterator;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public Entry<K, V> next() {
            current = iterator.next();
            return new SimpleEntry<K, V>(current.getKey(), current.getValue().getValue());
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            ConcurrentLinkedHashMap.this.remove(current.getKey(), current.getValue());
            current = null;
        }
    }

    /**
     * This duplicates {@link java.util.AbstractMap.SimpleEntry} until the class is made accessible.
     * Update: SimpleEntry is public in JDK 6.
     */
    private static final class SimpleEntry<K, V> implements Entry<K, V> {
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            return eq(key, entry.getKey()) && eq(value, entry.getValue());
        }

        public int hashCode() {
            return ((key == null) ? 0 : key.hashCode()) ^
                    ((value == null) ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }

        private static boolean eq(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : o1.equals(o2);
        }
    }
}
