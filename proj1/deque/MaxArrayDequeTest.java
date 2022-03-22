package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

class MyComparator implements Comparator<String> {
    public int compare(String a, String b)
    {
        if(a.compareTo(b)>0)
            return 1;
        else if(a.compareTo(b)<0)
            return -1;
        else
            return 0;
    }
}

public class MaxArrayDequeTest {

    @Test
    public void SimpleMaxArrayTest() {

        MyComparator myComparator = new MyComparator();

        MaxArrayDeque<String> lld1 = new MaxArrayDeque<String>(myComparator);

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();

        System.out.println(lld1.max());
        assertTrue("", lld1.max().equals("middle"));
    }
}