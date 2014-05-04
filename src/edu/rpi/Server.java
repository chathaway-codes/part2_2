package edu.rpi;

import java.io.*;
import java.lang.Thread.*;
import java.util.HashSet;


public class Server {
    private static final int A = Constants.A;
    private static final int Z = Constants.Z;
    private static final int numLetters = Constants.numLetters;
    private static Account[] accounts;

    private static void dumpAccounts() {
        // output values:
        for (int i = A; i <= Z; i++) {
            System.out.print("    ");
            if (i < 10) System.out.print("0");
            System.out.print(i + " ");
            System.out.print(new Character((char) (i + 'A')) + ": ");
            accounts[i].print();
            System.out.print(" (");
            accounts[i].printMod();
            System.out.print(")\n");
        }
    }

    public static void main (String args[])
        throws IOException {
        accounts = new Account[numLetters];
        for (int i = A; i <= Z; i++) {
            accounts[i] = new Account(Z-i);
        }

        // read transactions from input file
        String line;
        BufferedReader input =
            new BufferedReader(new FileReader(args[0]));

// TO DO: you will need to create an Executor and then modify the
// following loop to feed tasks to the executor instead of running them
// directly.  Don't modify the initialization of accounts above, or the
// output at the end.

        while ((line = input.readLine()) != null) {
            Worker w = new Worker(accounts, line);
            w.run();
        }

        System.out.println("final values:");
        dumpAccounts();
    }
}