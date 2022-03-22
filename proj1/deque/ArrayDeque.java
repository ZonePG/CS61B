package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    private void resetIndex(int newNexFirst, int newNextLast) {
        this.nextFirst = newNexFirst;
        this.nextLast = newNextLast;
    }

    /**
     * Creates an empty SLList.
     */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        resetIndex(4, 5);
    }

    private int getFirstIndex() {
        return (nextFirst + 1) % items.length;
    }

    private int getLastIndex() {
        return (nextLast - 1 + items.length) % items.length;
    }

    private T getFirst() {
        return items[getFirstIndex()];
    }

    private T getLast() {
        return items[getLastIndex()];
    }

    private void decNextFirst() {
        nextFirst = (nextFirst + items.length - 1) % items.length;
    }

    private void incNextFirst() {
        nextFirst = (nextFirst + 1) % items.length;
    }

    private void decNextLast() {
        nextLast = (nextLast + items.length - 1) % items.length;
    }

    private void incNextLast() {
        nextLast = (nextLast + 1) % items.length;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int first = getFirstIndex();
        int last = getLastIndex();
        if (first < last) {
            System.arraycopy(items, first, a, 0, size);
        } else {
            if (first <= items.length - 1) {
                System.arraycopy(items, first, a, 0, items.length - first);
            }
            if (last >= 0) {
                System.arraycopy(items, 0, a, items.length - first, last + 1);
            }
        }
        items = a;
        resetIndex(items.length - 1, size);
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        items[nextFirst] = item;
        decNextFirst();
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        items[nextLast] = item;
        incNextLast();
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    private void checkResize() {
        if ((size < items.length / 4 + 1) && (items.length > 16)) {
            resize(items.length / 2);
        }
    }

    /**
     * Deletes and returns last item.
     */
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        checkResize();

        T x = getFirst();
        items[getFirstIndex()] = null;
        incNextFirst();
        size -= 1;
        return x;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }

        checkResize();
        T x = getLast();
        items[getLastIndex()] = null;
        decNextLast();
        size -= 1;
        return x;
    }

    @Override
    public T get(int index) {
        return items[(nextFirst + 1 + index) % items.length];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator<T> implements Iterator<T> {
        private int pos;

        ArrayDequeIterator() {
            pos = 0;
        }

        public boolean hasNext() {
            return pos < size;
        }

        public T next() {
            T returnItem = (T) get(pos);
            pos += 1;
            return returnItem;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<T> other = (Deque<T>) o;
        if (size() != other.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            T item1 = get(i);
            T item2 = other.get(i);
            if (!item1.equals(item2)) {
                return false;
            }
        }
        return true;
    }
}
