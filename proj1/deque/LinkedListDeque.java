package deque;

public class LinkedListDeque<T> {
    private class Node {
        public T item;
        public Node prev;
        public Node next;

        public Node(T item, Node prev, Node next) {
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

    public void addFirst(T item) {
        Node prevFirstNode = sentienel.next;
        Node newFirstNode = new Node(item, sentienel, prevFirstNode);
        prevFirstNode.prev = newFirstNode;
        sentienel.next = newFirstNode;
        size += 1;
    }

    public void addLast(T item) {
        Node prevLastNode = sentienel.prev;
        Node newLastNode = new Node(item, prevLastNode, sentienel);
        prevLastNode.next = newLastNode;
        sentienel.prev = newLastNode;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

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

    public T get(int index) {
        Node p = getFirstNode();
        while (p != sentienel) {
            if (index == 0) {
                return p.item;
            }
            index -= 1;
            p.next = p;
        }
        return null;
    }

    public T getRecursiveHelper(int index, Node p) {
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
}
