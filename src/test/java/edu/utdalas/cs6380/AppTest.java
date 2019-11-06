package edu.utdalas.cs6380;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * classical mcnf graph (n=4)
     */
    @Test
    public void testCase1() {
        String[] args = new String[]{"input/connectivity1.txt"};
        App.main(args);
        assertEquals(3, MasterThread.leaderID);
    }

    /**
     * some random graph (n=6)
     */
    @Test
    public void testCase2() {
        String[] args = new String[]{"input/connectivity2.txt"};
        App.main(args);
        assertEquals(5, MasterThread.leaderID);
    }

    /**
     * single node (n=1)
     */
    @Test
    public void testCase3() {
        String[] args = new String[]{"input/connectivity3.txt"};
        App.main(args);
        assertEquals(1, MasterThread.leaderID);
    }
}