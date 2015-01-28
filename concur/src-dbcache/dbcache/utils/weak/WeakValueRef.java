/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package dbcache.utils.weak;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * Weak value ref.
 *
 * @author  <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @param <K> the key type
 * @param <V> the value type
 */
class WeakValueRef<K, V> extends WeakReference<V> implements ValueRef<K, V>
{
   /**
    * The key
    */
   public K key;

   /**
    * Safely create a new WeakValueRef
    *
    * @param <K> the key type
    * @param <V> the value type
    * @param key the key
    * @param val the value
    * @param q   the reference queue
    * @return the reference or null if the value is null
    */
   static <K, V> WeakValueRef<K, V> create(K key, V val, ReferenceQueue<V> q)
   {
      if (val == null)
         return null;
      else
         return new WeakValueRef<K, V>(key, val, q);
   }

   /**
    * Create a new WeakValueRef.
    *
    * @param key the key
    * @param val the value
    * @param q   the reference queue
    */
   private WeakValueRef(K key, V val, ReferenceQueue<V> q)
   {
      super(val, q);
      this.key = key;
   }

   public K getKey()
   {
      return key;
   }

   public V getValue()
   {
      return get();
   }

   public V setValue(V value)
   {
      throw new UnsupportedOperationException("setValue");
   }

   @Override
   public String toString()
   {
      return String.valueOf(get());
   }
}