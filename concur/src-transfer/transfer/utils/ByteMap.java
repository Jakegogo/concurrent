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
package transfer.utils;

/**
 * for concurrent ByteMap
 *
 * @author jake
 */
@SuppressWarnings("unchecked")
public class ByteMap<V> {

    public static final int     DEFAULT_TABLE_SIZE = 1024;

    private final Entry<V>[] buckets;
    private final int           indexMask;

    public ByteMap(){
        this(DEFAULT_TABLE_SIZE);
    }

    public ByteMap(int tableSize){
        this.indexMask = tableSize - 1;
        this.buckets = new Entry[tableSize];
    }

    public final V get(byte key) {
        final int bucket = key & indexMask;

        for (Entry<V> entry = buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                return (V) entry.value;
            }
        }

        return null;
    }

    public boolean put(byte key, V value) {
        final int bucket = key & indexMask;

        for (Entry<V> entry = buckets[bucket]; entry != null; entry = entry.next) {
            if (key == entry.key) {
                entry.value = value;
                return true;
            }
        }

        Entry<V> entry = new Entry<V>(key, value, buckets[bucket]);
        buckets[bucket] = entry;  // 并发是处理时会可能导致缓存丢失，但不影响正确性

        return false;
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < buckets.length; ++i) {
            for (Entry<V> entry = buckets[i]; entry != null; entry = entry.next) {
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

    protected static final class Entry<V> {

        public final byte     key;
        public V     value;

        public final Entry<V> next;

        public Entry(byte key, V value, Entry<V> next){
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

}
