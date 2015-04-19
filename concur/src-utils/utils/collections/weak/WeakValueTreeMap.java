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
package utils.collections.weak;

import java.lang.ref.ReferenceQueue;
import java.util.Comparator;
import java.util.SortedMap;

/**
 * This Map will remove entries when the value in the map has been
 * cleaned from garbage collection
 *
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class WeakValueTreeMap<K, V> extends ReferenceValueTreeMap<K, V>
{
   public WeakValueTreeMap()
   {
   }

   public WeakValueTreeMap(Comparator<K> kComparator)
   {
      super(kComparator);
   }

   public WeakValueTreeMap(SortedMap<K, ValueRef<K, V>> sorted)
   {
      super(sorted);
   }

   protected ValueRef<K, V> create(K key, V value, ReferenceQueue<V> q)
   {
      return WeakValueRef.create(key, value, q);
   }
}