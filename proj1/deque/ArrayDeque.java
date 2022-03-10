package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    private void resetIndex(int nextFirst, int nextLast) {
        this.nextFirst = nextFirst;
        this.nextLast = nextLast;
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
        if (nextFirst < nextLast && size < items.length) {
            System.arraycopy(items, getFirstIndex(), a, 0, size);
        } else {
            if (nextFirst < items.length - 1) {
                System.arraycopy(items, getFirstIndex(), a, 0, items.length - getFirstIndex());
            }
            if (nextLast > 0) {
                System.arraycopy(items, 0, a, items.length - getFirstIndex(), getLastIndex() + 1);
            }
        }
        items = a;
        resetIndex(items.length - 1, size);
    }

    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        items[nextFirst] = item;
        decNextFirst();
        size += 1;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }

        items[nextLast] = item;
        incNextLast();
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    private void check_resize() {
        if ((size < items.length / 4 + 1) && (size > 16)) {
            resize(items.length / 2);
        }
    }

    /**
     * Deletes and returns last item.
     */
    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        check_resize();

        T x = getFirst();
        items[getFirstIndex()] = null;
        incNextFirst();
        size -= 1;
        return x;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }

        check_resize();
        T x = getLast();
        items[getLastIndex()] = null;
        decNextLast();
        size -= 1;
        return x;
    }

    public T get(int index) {
        return items[(nextFirst + 1 + index) % items.length];
    }
}
