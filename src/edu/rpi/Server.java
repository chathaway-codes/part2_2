package edu.rpi;

import java.io.*;
import java.lang.Thread.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Server {
    private static final int A = Constants.A;
    private static final int Z = Constants.Z;
    private static final int numLetters = Constants.numLetters;
    private static Account[] accounts;
    public static boolean verbose = true;

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
    
    public static void auto(String []args) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	if(args.length != 2)
    		return;
    	verbose = false;
    	String line;
        BufferedReader input =
            new BufferedReader(new FileReader(args[1]));
        
        ArrayList<String> lines = new ArrayList<String>();

        while ((line = input.readLine()) != null) {
        	lines.add(line);
        }
        
        input.close();
        
        long []delays = new long[]{100, 50, 200};
        String []commands = new String[]{"newFixedThreadPool", "newFixedThreadPool", "newFixedThreadPool", "newFixedThreadPool", "newCachedThreadPool"};
        Object [][]arguments = new Object[][]{{26}, {10}, {6}, {1}, {}};
        Method []threadPoolMethods = new Method[]{Executors.class.getDeclaredMethod("newFixedThreadPool", Integer.TYPE), 
        		Executors.class.getDeclaredMethod("newFixedThreadPool", Integer.TYPE), 
        		Executors.class.getDeclaredMethod("newFixedThreadPool", Integer.TYPE), 
        		Executors.class.getDeclaredMethod("newFixedThreadPool", Integer.TYPE),
        		Executors.class.getDeclaredMethod("newCachedThreadPool")};
        //ExecutorService []executors = new ExecutorService[]{Executors.newCachedThreadPool(),Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(6), Executors.newFixedThreadPool(10)};
        
        for(Long delay : delays) {
        	Account.delay = delay;
        	for(int i=0; i < threadPoolMethods.length; i++) {
        		ExecutorService executor = (ExecutorService) threadPoolMethods[i].invoke(null, arguments[i]);
                ArrayList<Worker> workers = new ArrayList<Worker>();
                Account []accounts = Server.genAccounts();
                for(String l : lines) {
                	workers.add(new Worker(accounts, l));
                }
                
                long startTime = System.nanoTime();
                
                run(executor, workers.toArray(new Worker[1]));
                
                long endTime = System.nanoTime();
                
                String out = String.format("%s(%d threads) (%d delay): %f ms execution time", commands[i], ((ThreadPoolExecutor)executor).getCorePoolSize(), delay, ((endTime-startTime)*1.0e-6));
                
                System.out.println(out);
        	}
        }
    }

    public static void main (String args[])
        throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // read transactions from input file
    	if(args.length == 2 && args[0].equals("--auto")) {
    		auto(args);
    		return;
    	}
        String line;
        BufferedReader input =
            new BufferedReader(new FileReader(args[0]));
        
        ArrayList<String> lines = new ArrayList<String>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ArrayList<Worker> workers = new ArrayList<Worker>();
        accounts = Server.genAccounts();

// TO DO: you will need to create an Executor and then modify the
// following loop to feed tasks to the executor instead of running them
// directly.  Don't modify the initialization of accounts above, or the
// output at the end.

        while ((line = input.readLine()) != null) {
        	workers.add(new Worker(accounts, line));
        }
        
        input.close();
        
        // Start timing things
        long startTime = System.nanoTime();
        
        run(executor, workers.toArray(new Worker[1]));
        
        long endTime = System.nanoTime();

        System.out.println("final values:");
        dumpAccounts();
        System.out.println("Elapsed time: " + ((endTime-startTime)*1.0e-6) + " ms");
    }
}