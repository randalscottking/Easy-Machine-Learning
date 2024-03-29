/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by RapidMiner and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapidminer.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package liblinear;


final class ArraySorter {

   /**
    * <p>Sorts the specified array of doubles into <b>descending</b> order.</p>
    *
    * <em>This code is borrowed from Sun's JDK 1.6.0.07</em>
    */
   public static void reversedMergesort( double[] a ) {
      reversedMergesort(a, 0, a.length);
   }

   private static void reversedMergesort( double x[], int off, int len ) {
      // Insertion sort on smallest arrays
      if ( len < 7 ) {
         for ( int i = off; i < len + off; i++ )
            for ( int j = i; j > off && x[j - 1] < x[j]; j-- )
               swap(x, j, j - 1);
         return;
      }

      // Choose a partition element, v
      int m = off + (len >> 1); // Small arrays, middle element
      if ( len > 7 ) {
         int l = off;
         int n = off + len - 1;
         if ( len > 40 ) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s);
            m = med3(x, m - s, m, m + s);
            n = med3(x, n - 2 * s, n - s, n);
         }
         m = med3(x, l, m, n); // Mid-size, med of 3
      }
      double v = x[m];

      // Establish Invariant: v* (<v)* (>v)* v*
      int a = off, b = a, c = off + len - 1, d = c;
      while ( true ) {
         while ( b <= c && x[b] >= v ) {
            if ( x[b] == v ) swap(x, a++, b);
            b++;
         }
         while ( c >= b && x[c] <= v ) {
            if ( x[c] == v ) swap(x, c, d--);
            c--;
         }
         if ( b > c ) break;
         swap(x, b++, c--);
      }

      // Swap partition elements back to middle
      int s, n = off + len;
      s = Math.min(a - off, b - a);
      vecswap(x, off, b - s, s);
      s = Math.min(d - c, n - d - 1);
      vecswap(x, b, n - s, s);

      // Recursively sort non-partition-elements
      if ( (s = b - a) > 1 ) reversedMergesort(x, off, s);
      if ( (s = d - c) > 1 ) reversedMergesort(x, n - s, s);
   }

   /**
    * Swaps x[a] with x[b].
    */
   private static void swap( double x[], int a, int b ) {
      double t = x[a];
      x[a] = x[b];
      x[b] = t;
   }

   /**
    * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
    */
   private static void vecswap( double x[], int a, int b, int n ) {
      for ( int i = 0; i < n; i++, a++, b++ )
         swap(x, a, b);
   }

   /**
    * Returns the index of the median of the three indexed doubles.
    */
   private static int med3( double x[], int a, int b, int c ) {
      return (x[a] < x[b] ? (x[b] < x[c] ? b : x[a] < x[c] ? c : a) : (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
   }


}
