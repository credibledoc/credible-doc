package com.credibledoc.substitution.core.pair;

import java.util.Objects;

/**
 * Contains a pair of objects.
 * 
 * @param <L> the left object
 * @param <R> the right object
 *           
 * @author Kyrylo Semenko
 */
public class Pair<L,R> {
    /**
     * Left value in a pair. Can be 'null'.
     */
    private L left;

    /**
     * Right value in a pair. Can be 'null'.
     */
    private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "Pair{" +
            "left=" + left +
            ", right=" + right +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getLeft(), pair.getLeft()) &&
            Objects.equals(getRight(), pair.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }

    /**
     * @return The {@link #left} field value.
     */
    public L getLeft() {
        return left;
    }

    /**
     * @param left see the {@link #left} field description.
     */
    public void setLeft(L left) {
        this.left = left;
    }

    /**
     * @return The {@link #right} field value.
     */
    public R getRight() {
        return right;
    }

    /**
     * @param right see the {@link #right} field description.
     */
    public void setRight(R right) {
        this.right = right;
    }
}
