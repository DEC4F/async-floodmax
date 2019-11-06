package edu.utdalas.cs6380;

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
    }

    /**
     * some random graph (n=6)
     */
    @Test
    public void testCase2() {
        String[] args = new String[]{"input/connectivity2.txt"};
        App.main(args);
    }

    /**
     * single node (n=1)
     */
    @Test
    public void testCase3() {
        String[] args = new String[]{"input/connectivity3.txt"};
        App.main(args);
    }
    
    @Test
    public void testCase4() {
        String[] args = new String[]{"input/connectivity4.txt"};
        App.main(args);
    }
    
    @Test
    public void testCase5() {
        String[] args = new String[]{"input/connectivity5.txt"};
        App.main(args);
    }

    @Test
    public void testCase6() {
        String[] args = new String[]{"input/connectivity6.txt"};
        App.main(args);
    }

    @Test
    public void testCase7() {
        String[] args = new String[]{"input/connectivity7.txt"};
        App.main(args);
    }

    @Test
    public void testCase8() {
        String[] args = new String[]{"input/connectivity8.txt"};
        App.main(args);
    }

    @Test
    public void testCase9() {
        String[] args = new String[]{"input/connectivity9.txt"};
        App.main(args);
    }

    @Test
    public void testCase10() {
        String[] args = new String[]{"input/connectivity10.txt"};
        App.main(args);
    }

    @Test
    public void testCase11() {
        String[] args = new String[]{"input/connectivity11.txt"};
        App.main(args);
    }

    @Test
    public void testCase12() {
        String[] args = new String[]{"input/connectivity12.txt"};
        App.main(args);
    }
}
