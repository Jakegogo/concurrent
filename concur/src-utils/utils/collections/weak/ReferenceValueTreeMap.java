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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This Map will remove entries when the value in the map has been
 * cleaned from garbage collection
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author  <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class ReferenceValueTreeMap<K, V> extends ReferenceValueMap<K, V>
{
   protected ReferenceValueTreeMap()
   {
   }

   protected ReferenceValueTreeMap(Comparator<K> comparator)
   {
      super(comparator);
   }

   protected ReferenceValueTreeMap(SortedMap<K, ValueRef<K, V>> sorted)
   {
      super(sorted);
   }

   protected Map<K, ValueRef<K, V>> createMap()
   {
      return new TreeMap<K, ValueRef<K,V>>();
   }

   protected Map<K, ValueRef<K, V>> createMap(Comparator<K> comparator)
   {
      return new TreeMap<K, ValueRef<K,V>>(comparator);
   }

   protected Map<K, ValueRef<K, V>> createMap(SortedMap<K, ValueRef<K, V>> map)
   {
      return new TreeMap<K, ValueRef<K,V>>(map);
   }

   protected Map<K, ValueRef<K, V>> createMap(int initialCapacity)
   {
      throw new UnsupportedOperationException("Cannot create TreeMap with such parameters.");
   }

   protected Map<K, ValueRef<K, V>> createMap(int initialCapacity, float loadFactor)
   {
      throw new UnsupportedOperationException("Cannot create TreeMap with such parameters.");
   }
}