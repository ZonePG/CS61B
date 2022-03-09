package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> aListNoResizing = new AListNoResizing<>();
        BuggyAList<Integer> buggyAList = new BuggyAList<>();

        aListNoResizing.addLast(4);
        buggyAList.addLast(4);

        aListNoResizing.addLast(5);
        buggyAList.addLast(5);

        aListNoResizing.addLast(6);
        buggyAList.addLast(6);

        assertEquals(aListNoResizing.size(), buggyAList.size());
        assertEquals(aListNoResizing.removeLast(), buggyAList.removeLast());
        assertEquals(aListNoResizing.removeLast(), buggyAList.removeLast());
        assertEquals(aListNoResizing.removeLast(), buggyAList.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> buggy  = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                buggy.addLast(randVal);
                assertEquals(correct.size(), buggy.size());
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1 && correct.size() > 0) {
                // getLast
                int lastVal_correct = correct.getLast();
                int lastVal_buggy = buggy.getLast();
                assertEquals(lastVal_correct, lastVal_buggy);
                System.out.println("getLast(" + lastVal_correct + ")");
            } else if (operationNumber == 2 && correct.size() > 0) {
                // removeLast
                int lastVal_correct = correct.removeLast();
                int lastVal_buggy = buggy.removeLast();
                assertEquals(correct.size(), buggy.size());
                assertEquals(lastVal_correct, lastVal_buggy);
                System.out.println("removeLast(" + lastVal_buggy + ")");
            }
        }
    }
}
