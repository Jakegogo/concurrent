/*
 * Copyright 1999-2101 Alibaba Group.
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

/**
 * for concurrent IdentityHashMap
 *
 * @author wenshao[szujobs@hotmail.com]
 */
@SuppressWarnings("unchecked")
public class IdentityHashMap<K, V> {

    public static final int     DEFAULT_TABLE_SIZE = 1024;

    private final Entry<K, V>[] buckets;
    private final int           indexMask;

    public IdentityHashMap(){
        this(DEFAULT_TABLE_SIZE);
    }

    public IdentityHashMap(int tableSize){
        this.indexMask = tableSize - 1;
        this.buckets = new Entry[tableSize];
    }

    public final V get(K key) {
        final int hash = System.identityHashCode(key);
        final int bucket = hash & indexMask;

        for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                return (V) entry.value;
            }
        }

        return null;
    }

    public boolean put(K key, V value) {
        final int hash = System.identityHashCode(key);
        final int bucket = hash & indexMask;

        for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                entry.value = value;
                return true;
            }
        }

        Entry<K, V> entry = new Entry<K, V>(key, value, hash, buckets[bucket]);
        buckets[bucket] = entry;  // 并发是处理时会可能导致缓存丢失，但不影响正确性

        return false;
    }
    
    // 保证唯一必须统一且只调此方法进行put
    public V putIfAbsent(K key, V value) {
    	final int hash = System.identityHashCode(key);
        final int bucket = hash & indexMask;
        
        synchronized (this) {
	        for (Entry<K, V> entry = buckets[bucket]; entry != null; entry = entry.next) {
	            if (key == entry.key) {
	            	V oldValue = entry.value;
	                entry.value = value;
	                return oldValue;
	            }
	        }
	
	        Entry<K, V> entry = new Entry<K, V>(key, value, hash, buckets[bucket]);
	        buckets[bucket] = entry;
        }

        return value;
	}

    public int size() {
        int size = 0;
        for (int i = 0; i < buckets.length; ++i) {
            for (Entry<K, V> entry = buckets[i]; entry != null; entry = entry.next) {
                size++;
            }
        }
        return size;
    }
    
    public void clear() {
		for( int i = 0; i < DEFAULT_TABLE_SIZE;i++) {
			buckets[i] = null;
		}
	}

    protected static final class Entry<K, V> {

        public final int   hashCode;
        public final K     key;
        public V     value;

        public final Entry<K, V> next;

        public Entry(K key, V value, int hash, Entry<K, V> next){
            this.key = key;
            this.value = value;
            this.next = next;
            this.hashCode = hash;
        }
    }

}
