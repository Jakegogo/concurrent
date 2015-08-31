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

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

/**
 * Convenience class to wrap an <tt>Object</tt> into a <tt>WeakReference</tt>.
 *
 * <p>Modified from <tt>java.util.WeakHashMap.WeakKey</tt>.
 *
 * @version <tt>$Revision: 2787 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@SuppressWarnings("unchecked")
public final class WeakObject
   extends WeakReference
{
   /** The hash code of the nested object */
   protected final int hashCode;

   /**
    * Construct a <tt>WeakObject</tt>.
    *
    * @param obj  Object to reference.
    */
   public WeakObject(final Object obj) {
      super(obj);
      hashCode = obj.hashCode();
   }

   /**
    * Construct a <tt>WeakObject</tt>.
    *
    * @param obj     Object to reference.
    * @param queue   Reference queue.
    */
   public WeakObject(final Object obj, final ReferenceQueue queue) {
      super(obj, queue);
      hashCode = obj.hashCode();
   }

   /**
    * Check the equality of an object with this.
    *
    * @param obj  Object to test equality with.
    * @return     True if object is equal.
    */
   public boolean equals(final Object obj) {
      if (obj == this) return true;

      if (obj != null && obj.getClass() == getClass()) {
         WeakObject soft = (WeakObject) obj;

         Object a = this.get();
         Object b = soft.get();
         if (a == null || b == null) return false;
         return a == b || a.equals(b);

      }

      return false;
   }

   /**
    * Return the hash code of the nested object.
    *
    * @return  The hash code of the nested object.
    */
   public int hashCode() {
      return hashCode;
   }


   /////////////////////////////////////////////////////////////////////////
   //                            Factory Methods                          //
   /////////////////////////////////////////////////////////////////////////

   /**
    * Create a <tt>WeakObject</tt> for the given object.
    *
    * @param obj     Object to reference.
    * @return        <tt>WeakObject</tt> or <tt>null</tt> if object is null.
    */
   public static WeakObject create(final Object obj) {
      if (obj == null) return null;
      else return new WeakObject(obj);
   }

   /**
    * Create a <tt>WeakObject</tt> for the given object.
    *
    * @param obj     Object to reference.
    * @param queue   Reference queue.
    * @return        <tt>WeakObject</tt> or <tt>null</tt> if object is null.
    */
   public static WeakObject create(final Object obj,
                                   final ReferenceQueue queue)
   {
      if (obj == null) return null;
      else return new WeakObject(obj, queue);
   }
}
