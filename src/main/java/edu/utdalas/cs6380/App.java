package edu.utdalas.cs6380;

import java.io.*;
import java.util.Arrays;

/**
 * This app simulates the behavior of processes in 
 * asyncrhonous floodmax algorithm using multi threading in Java
 *
 * Usage:
 * mvn test
 *
 * Positional:
 * input file path     -     file path to the input.dat
 */
public final class App {

    private static int n;
    private static int[] ids;
    private static int[][] adj;

    private App() {
    }

    //////////////////////////////////
    // MAIN
    //////////////////////////////////

    /**
     * create and run the master thread
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        try {
            validateInput(args);
            String inputLoc = args[0];
            readInput(inputLoc);
            MasterThread master = new MasterThread(n, ids, adj);
            new Thread(master).start();

            // test hook
            while (true) {
                if (master.getFinished())
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////
    // HELPER
    //////////////////////////////////

    /**
     * reads input.dat for n and ids
     */
    private static void readInput(String inputPath) throws IOException {
        FileReader fr = new FileReader(new File(inputPath));
        BufferedReader br = new BufferedReader(fr);
        String line;
        int lineNum = 0;
        while ((line = br.readLine()) != null) {
            if (lineNum == 0) {
                n = Integer.parseInt(line);
                adj = new int[n][n];
            }
            else if (lineNum == 1)
                ids = Arrays.stream(line.split("\\s+")).map(String::trim).mapToInt(Integer::parseInt).toArray();
            else {
                adj[lineNum-2] = Arrays.stream(line.split("\\s+")).map(String::trim).mapToInt(Integer::parseInt).toArray();
            }
            lineNum ++;
        }

        br.close();
    }

    //////////////////////////////////
    // VALIDATION
    //////////////////////////////////

    private static void validateInput(String[] args) throws IllegalArgumentException {
        if (args.length != 1) {
            throw new IllegalArgumentException ("Must provide exactly one argument -- path to connectivity.txt");
        }
    }
}
