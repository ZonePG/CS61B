package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        private T item;
        private Node prev;
        private Node next;

        Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    /* The first item (if it exists) is at sentinel.next. */
    private Node sentienel;
    private int size;

    /**
     * Creates an empty SLList.
     */
    public LinkedListDeque() {
        sentienel = new Node(null, null, null);
        sentienel.prev = sentienel;
        sentienel.next = sentienel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        Node prevFirstNode = sentienel.next;
        Node newFirstNode = new Node(item, sentienel, prevFirstNode);
        prevFirstNode.prev = newFirstNode;
        sentienel.next = newFirstNode;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        Node prevLastNode = sentienel.prev;
        Node newLastNode = new Node(item, prevLastNode, sentienel);
        prevLastNode.next = newLastNode;
        sentienel.prev = newLastNode;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node p = sentienel.next;
        while (p != sentienel) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    private Node getFirstNode() {
        return sentienel.next;
    }

    /**
     * Deletes and returns last item.
     */
    @Override
    public T removeFirst() {
        Node prevFirstNode = getFirstNode();
        if (prevFirstNode == sentienel) {
            return null;
        }

        Node newFirstNode = prevFirstNode.next;
        T item = prevFirstNode.item;
        sentienel.next = newFirstNode;
        newFirstNode.prev = sentienel;
        size -= 1;
        return item;
    }

    private Node getLastNode() {
        return sentienel.prev;
    }

    @Override
    public T removeLast() {
        Node prevLastNode = getLastNode();
        if (prevLastNode == sentienel) {
            return null;
        }

        Node newLastNode = prevLastNode.prev;
        T item = prevLastNode.item;
        sentienel.prev = newLastNode;
        newLastNode.next = sentienel;
        size -= 1;
        return item;
    }

    @Override
    public T get(int index) {
        Node p = getFirstNode();
        while (p != sentienel) {
            if (index == 0) {
                return p.item;
            }
            index -= 1;
            p = p.next;
        }
        return null;
    }

    private T getRecursiveHelper(int index, Node p) {
        if (p == sentienel) {
            return null;
        }
        if (index == 0) {
            return p.item;
        }
        return getRecursiveHelper(index - 1, p.next);
    }

    public T getRecursive(int index) {
        return getRecursiveHelper(index, sentienel.next);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node iterNode;

        LinkedListDequeIterator() {
            iterNode = sentienel.next;
        }

        public boolean hasNext() {
            return iterNode != sentienel;
        }

        public T next() {
            T returnItem = iterNode.item;
            iterNode = iterNode.next;
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
