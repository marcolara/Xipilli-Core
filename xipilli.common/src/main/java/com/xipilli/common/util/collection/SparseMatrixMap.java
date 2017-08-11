package com.xipilli.common.util.collection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Two-dimensional map based on a HashMap of Pairs.
 */
public final class SparseMatrixMap<R, C, V> implements Serializable {

    /**
     * Last mod: 2011.01.05
     */
    private static final long serialVersionUID = 7550239539735293491L;

    // The map where all matrix elements are stored.
    private final Map<Pair<R,C>,V> matrixMap;


    public SparseMatrixMap() {
        matrixMap = new HashMap<Pair<R,C>,V>();
    }

    public SparseMatrixMap(int initialRowCapacity) {
        matrixMap = new HashMap<Pair<R,C>,V>(initialRowCapacity);
    }

    /**
     * Returns the value found at the specified row and column of the matrix.
     * Returns null if there's no value for the specified row and column.
     *
     * @param row Object
     * @param col Object
     * @return Object
     */
    public V get(R row, C col) {
        return matrixMap.get(new Pair<R,C>(row, col));
    }

    /**
     * Sets the value of the matrix at the specified row and column.
     *
     * @param row Object
     * @param col Object
     * @param value
     */
    public void put(R row, C col, V value) {
        if (row == null || col == null) {
            throw new IllegalArgumentException("Row or column may not be null.");
        }

        matrixMap.put(new Pair<R,C>(row, col), value);
    }

    public void remove(R row, C col) {
        matrixMap.remove(new Pair<R,C>(row, col));
    }

    /**
     * Returns a Set of all used "columns" in the matrix.
     *
     * @return Set
     */
    public Set<C> colSet() {
        Set<C> colSet = new HashSet<C>();

        for (Iterator<Pair<R,C>> iterator = matrixMap.keySet().iterator(); iterator.hasNext();) {
            Pair<R,C> pair = iterator.next();
            colSet.add(pair.getSecond());
        }

        return colSet;
    }

    /**
     * Returns a Set of all used "rows" in the matrix.
     *
     * @return Set
     */
    public Set<R> rowSet() {
        Set<R> rowSet = new HashSet<R>();

        for (Iterator<Pair<R,C>> iterator = matrixMap.keySet().iterator(); iterator.hasNext();) {
            Pair<R,C> pair = iterator.next();
            rowSet.add(pair.getFirst());
        }

        return rowSet;
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SparseMatrixMap)) {
            return false;
        }
        SparseMatrixMap other = (SparseMatrixMap) obj;
        return this.matrixMap.equals(other.matrixMap);
    }

    public int hashCode() {
        return matrixMap.hashCode();
    }


    /**
     * A Pair is a container for two objects, the "first" and the "second" one.
     * Instances of <code>Pair</code> are immutable.
     */
    private static class Pair<R,C> implements Serializable {

        /**
         * Last mod: 2011.01.05
         */
        private static final long serialVersionUID = 4408887947825924021L;

        private final R first;
        private final C second;

        /**
         * Constructor for Pair.
         * @param first must be different from null
         * @param second must be different from null
         */
        public Pair(R first, C second) {
            assert first != null;
            assert second != null;
            this.first = first;
            this.second = second;
        }

        /**
         * Returns the first.
         * @return Object
         */
        public R getFirst() {
            return first;
        }

        /**
         * Returns the second.
         * @return Object
         */
        public C getSecond() {
            return second;
        }

        /**
         * @see java.lang.Object#equals(Object)
         */
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Pair)) {
                return false;
            }
            Pair pair = (Pair) obj;
            return first.equals(pair.first) && second.equals(pair.second);
        }

        /*
         * @see java.lang.Object#hashCode()
         * KKB, 17.3.04: hashCode calculated after "Effective Java", Item 8
         */
        public int hashCode() {
            int result = 17;
            result = 37 * result + first.hashCode();
            result = 37 * result + second.hashCode();
            return result;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "(" + first + ", " + second + ")";
        }

    }
}