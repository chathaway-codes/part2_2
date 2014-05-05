package edu.rpi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Worker implements Runnable {
    private static final int A = Constants.A;
    private static final int Z = Constants.Z;
    private static final int numLetters = Constants.numLetters;
    
    public static HashMap<Thread, Runnable> workers = new HashMap<Thread, Runnable>();
    private long start_time;

    private AccountCache[] accounts;
    private Account[] allAccounts;
    private String transaction;

    // TO DO: The sequential version of Worker peeks at accounts
    // whenever it needs to get a value, and opens, updates, and closes
    // an account whenever it needs to set a value.  This won't work in
    // the parallel version.  Instead, you'll need to cache values
    // you've read and written, and then, after figuring out everything
    // you want to do, (1) open all accounts you need, for reading,
    // writing, or both, (2) verify all previously peeked-at values,
    // (3) perform all updates, and (4) close all opened accounts.

    public Worker(Account[] allAccounts, String trans) {
    	this.allAccounts = allAccounts;
    	accounts = new AccountCache[allAccounts.length];
    	for(int i=0; i < allAccounts.length; i++) {
    		accounts[i] = new AccountCache(allAccounts[i], i);
    	}
        transaction = trans;
    }
    
    // TO DO: parseAccount currently returns a reference to an account.
    // You probably want to change it to return a reference to an
    // account *cache* instead.
    //
    private AccountCache parseAccount(String name) {
        int accountNum = (int) (name.charAt(0)) - (int) 'A';
        if (accountNum < A || accountNum > Z)
            throw new InvalidTransactionError();
        AccountCache a = accounts[accountNum];
        for (int i = 1; i < name.length(); i++) {
            if (name.charAt(i) != '*')
                throw new InvalidTransactionError();
            accountNum = (accounts[accountNum].getValue() % numLetters);
            a = accounts[accountNum];
        }
        return a;
    }

    private int parseAccountOrNum(String name) {
        int rtn;
        if (name.charAt(0) >= '0' && name.charAt(0) <= '9') {
            rtn = new Integer(name).intValue();
        } else {
        	AccountCache account = parseAccount(name);
            rtn = parseAccount(name).getValue();
        }
        return rtn;
    }

    public void run() {
    	// If I'm not in the list, add myself
    	synchronized(this.workers) {
			Thread meThread = Thread.currentThread();
    		if(!workers.containsKey(meThread)) {
    			workers.put(meThread, this);
    			start_time = System.nanoTime();
    		}
    	}
        // tokenize transaction
        String[] commands = transaction.split(";");

        for (int i = 0; i < commands.length; i++) {
            String[] words = commands[i].trim().split("\\s");
            if (words.length < 3)
                throw new InvalidTransactionError();
            AccountCache lhs = parseAccount(words[0]);
            if (!words[1].equals("="))
                throw new InvalidTransactionError();
            int rhs = parseAccountOrNum(words[2]);
            for (int j = 3; j < words.length; j+=2) {
                if (words[j].equals("+")) {
                    rhs += parseAccountOrNum(words[j+1]);
                }
                else if (words[j].equals("-")) {
                    rhs -= parseAccountOrNum(words[j+1]);
                }
                else
                    throw new InvalidTransactionError();
            }
            
            lhs.setValue(rhs);
            //System.out.println("Updated " + words[0] + " equal tp " + lhs.getValue());
        }
        
        // Remove all unimportant things, then sort
        ArrayList<AccountCache> caches = new ArrayList<AccountCache>();
        for(AccountCache account : accounts) {
        	if(!account.action.equals("Y")) {
        		continue;
        	}
        	caches.add(account);
        }
    	Collections.sort(caches);
    	
		try {
	    	// Open all accounts
	    	for(AccountCache account : caches) {
	    		account.open();
	    	}
	    	// Commit all changes
	    	for(AccountCache account : caches) {
	    		account.commit();
	    	}
	    	// Close
	    	for(AccountCache account : caches) {
	    		account.close();
	    	}
		} catch (TransactionAbortException e) {
			// If we get here, it means we couldn't open something
			//  OR something was changed from when we peaked at it 
			//    until we opened
			
			// Make sure to close all accounts
			try {
				for(AccountCache account : caches) {
					account.close();
				}
			} catch (TransactionUsageError ex) {
				// This means the account was never opened; we just to make sure
				//  to try and close everything
			}
			
			// Delay before running again
			try {
				Thread.sleep(e.delay/1000000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	    	accounts = new AccountCache[allAccounts.length];
	    	for(int i=0; i < allAccounts.length; i++) {
	    		accounts[i] = new AccountCache(allAccounts[i], i);
	    	}
	    	
			run();
			return;
		}
        
        //System.out.println("commit: " + transaction);
    }
	
	public Long getRemainingTime() {
		long elapsed_time = System.nanoTime() - this.start_time;
		// Easier counting, because I'm lazy
		//  Total number of "operations", minus the amount of whitespace, minus the = sign
		int counter=0;
		for(char s : transaction.toCharArray()) {
			if(s != ' ' && s != '=' && (s < '0' || (int)s > '9') && s != '+' && s != '-') {
				counter++;
			}
		}
		long total_time = counter*Account.delay*1000000;
		long remainder_time = total_time - elapsed_time;
		if(remainder_time > 0)
			return remainder_time;
		return 0L;
	}
}