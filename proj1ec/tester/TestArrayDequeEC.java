package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;

import edu.princeton.cs.introcs.StdRandom;

public class TestArrayDequeEC {

    @Test
    public void test() {
        StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> sad2 = new ArrayDequeSolution<>();
        String errString = new String();

        for (int i = 0; i < 10; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                sad1.addLast(i);
                sad2.addLast(i);
                errString += ("addLast(" + i + ")\n");
            } else {
                sad1.addFirst(i);
                sad2.addFirst(i);
                errString += ("addFirst(" + i + ")\n");
            }
        }

        // sad1.printDeque();
        // System.out.println(sad2.toString());
        errString += ("size()\n");
        assertEquals(errString, sad2.size(), sad1.size());

        for (int i = 0; i < 10; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.5) {
                Integer item1 = sad1.removeLast();
                Integer item2 = sad2.removeLast();
                errString += ("removeLast()\n");
                assertEquals(errString, item2, item1);
            } else {
                Integer item1 = sad1.removeFirst();
                Integer item2 = sad2.removeFirst();
                errString += ("removeFirst()\n");
                assertEquals(errString, item2, item1);
            }
            errString += ("size()\n");
            assertEquals(errString, sad2.size(), sad1.size());
        }
    }
}
