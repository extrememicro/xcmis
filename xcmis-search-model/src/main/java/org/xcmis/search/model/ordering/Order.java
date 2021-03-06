/*
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
package org.xcmis.search.model.ordering;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: Order.java 2 2010-02-04 17:21:49Z andrew00x $
 */
public enum Order {
   ASCENDING("ASC"), DESCENDING("DESC");

   private final String symbol;

   private Order(String symbol)
   {
      this.symbol = symbol;
   }

   /**
    * Get the symbolic representation of the order.
    * 
    * @return the symbolic representation; never null
    */
   public String getSymbol()
   {
      return symbol;
   }

   /**
    * {@inheritDoc}
    * 
    * @see java.lang.Enum#toString()
    */
   @Override
   public String toString()
   {
      return symbol;
   }

   /**
    * Attempt to find the Order given a symbol. The matching is done independent of case.
    * 
    * @param symbol the symbol
    * @return the Order having the supplied symbol, or null if there is no Order with the supplied symbol
    * @throws IllegalArgumentException if the symbol is null
    */
   public static Order forSymbol(String symbol)
   {
      if (ASCENDING.getSymbol().equalsIgnoreCase(symbol))
      {
         return ASCENDING;
      }
      if (DESCENDING.getSymbol().equalsIgnoreCase(symbol))
      {
         return DESCENDING;
      }
      return null;
   }

}
