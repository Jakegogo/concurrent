/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * A <tt>Set</tt> implementation with <em>weak elements</em>.  An entry in
 * a <tt>WeakSet</tt> will automatically be removed when the element is no
 * longer in ordinary use.  More precisely, the presence of an given element
 * will not prevent the element from being discarded by the garbage collector,
 * that is, made finalizable, finalized, and then reclaimed.
 *
 * @version <tt>$Revision: 2787 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public class WeakSet
   extends AbstractSet
   implements Set
{
   /** The reference queue used to get object removal notifications. */
   protected final ReferenceQueue queue = new ReferenceQueue();

   /** The <tt>Set</tt> which will be used for element storage. */
   protected final Set set;

   /**
    * Construct a <tt>WeakSet</tt>.  Any elements in the given set will be
    * wrapped in {@link WeakObject} references.
    *
    * @param set  The <tt>Set</tt> which will be used for element storage.
    *
    * @throws NullArgumentException    Set is <tt>null</tt>.
    */
   public WeakSet(final Set set) {
      if (set == null)
         throw new IllegalArgumentException("set");

      // reset any elements to weak objects
      if (set.size() != 0) {
         Object elements[] = set.toArray();
         set.clear();

         for (Object element : elements) {
            add(element);
         }
      }

      this.set = set;
   }

   /**
    * Construct a <tt>WeakSet</tt> based on a <tt>HashSet</tt>.
    */
   public WeakSet() {
      this(new HashSet());
   }

   /**
    * Maintain the elements in the set.  Removes objects from the set that
    * have been reclaimed due to GC.
    */
   protected final void maintain() {
      WeakObject weak;
      while ((weak = (WeakObject)queue.poll()) != null) {
         set.remove(weak);
      }
   }

   /**
    * Return the size of the set.
    *
    * @return  The size of the set.
    */
   public int size() {
      maintain();

      return set.size();
   }

   /**
    * Return an iteration over the elements in the set.
    *
    * @return  An iteration over the elements in the set.
    */
   public Iterator iterator() {
      return new Iterator() {

            /** The set's iterator */
            Iterator iter = set.iterator();

            /** JBCOMMON-24, handle null values and multiple invocations of hasNext() */
            Object UNKNOWN = new Object();

            /** The next available object. */
            Object next = UNKNOWN;

            public boolean hasNext() {
               if (next != UNKNOWN) {
                  return true;
               }
               while (iter.hasNext()) {
                  WeakObject weak = (WeakObject)iter.next();
                  Object obj = null;
                  if (weak != null && (obj = weak.get()) == null) {
                     // object has been reclaimed by the GC
                     continue;
                  }
                  next = obj;
                  return true;
               }
               return false;
            }

            public Object next() {
               if ((next == UNKNOWN) && !hasNext()) {
                  throw new NoSuchElementException();
               }
               Object obj = next;
               next = UNKNOWN;

               return obj;
            }

            public void remove() {
               iter.remove();
            }
         };
   }

   /**
    * Add an element to the set.
    *
    * @param obj  Element to add to the set.
    * @return     True if the element was added.
    */
   public boolean add(final Object obj) {
      maintain();

      return set.add(WeakObject.create(obj, queue));
   }

   /**
    * Returns <tt>true</tt> if this set contains no elements.
    *
    * @return  <tt>true</tt> if this set contains no elements.
    */
   public boolean isEmpty() {
      maintain();

      return set.isEmpty();
   }

   /**
    * Returns <tt>true</tt> if this set contains the specified element.
    *
    * @param obj  Element whose presence in this set is to be tested.
    * @return     <tt>true</tt> if this set contains the specified element.
    */
   public boolean contains(final Object obj) {
      maintain();

      return set.contains(WeakObject.create(obj));
   }

   /**
    * Removes the given element from this set if it is present.
    *
    * @param obj  Object to be removed from this set, if present.
    * @return     <tt>true</tt> if the set contained the specified element.
    */
   public boolean remove(final Object obj) {
      maintain();

      return set.remove(WeakObject.create(obj));
   }

   /**
    * Removes all of the elements from this set.
    */
   public void clear() {
      set.clear();
   }

   /**
     * Returns a shallow copy of this <tt>WeakSet</tt> instance: the elements
     * themselves are not cloned.
     *
     * @return    A shallow copy of this set.
     */
   public Object clone() {
      maintain();

      try {
         return super.clone();
      }
      catch (CloneNotSupportedException e) {
         throw new InternalError();
      }
   }
}
