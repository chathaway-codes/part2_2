package edu.rpi;

import java.io.*;
import java.lang.Thread.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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
    
    public static Account []genAccounts() {
    	ArrayList<Account> accounts = new ArrayList<Account>(numLetters);
        for (int i = A; i <= Z; i++) {
        	accounts.add(new Account(Z-i));
        }
    	return accounts.toArray(new Account[1]);
    }
    
    public static void run(ExecutorService executor, Worker []workers) {
    	// Add all the workers to the executor
    	for(Worker w : workers) {
    		executor.execute(w);
    	}

        executor.shutdown(); // Disable new tasks from being submitted
        try {
          // Wait a while for existing tasks to terminate
          if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
            executor.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            if (!executor.awaitTermination(60, TimeUnit.SECONDS))
                System.err.println("Pool did not terminate");
          }
        } catch (InterruptedException ie) {
          // (Re-)Cancel if current thread also interrupted
          executor.shutdownNow();
          // Preserve interrupt status
          Thread.currentThread().interrupt();
        }
    }

    public static void main (String args[])
        throws IOException {
        accounts = genAccounts();

        // read transactions from input file
        String line;
        BufferedReader input =
            new BufferedReader(new FileReader(args[0]));
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Worker> workers = new ArrayList<Worker>();

// TO DO: you will need to create an Executor and then modify the
// following loop to feed tasks to the executor instead of running them
// directly.  Don't modify the initialization of accounts above, or the
// output at the end.

        while ((line = input.readLine()) != null) {
        	workers.add(new Worker(accounts, line));
        }
        
        input.close();
        
        run(executor, workers.toArray(new Worker[1]));

        System.out.println("final values:");
        dumpAccounts();
    }
}