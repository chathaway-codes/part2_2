package edu.rpi.test;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import edu.rpi.Account;
import edu.rpi.Server;
import edu.rpi.Worker;

public class RotateTest {
	
	public static Integer getValue(Account account) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Field f = account.getClass().getDeclaredField("value");
    	f.setAccessible(true);
    	Object _value = f.get(account);
    	Integer value = (Integer)_value;
    	return value;
	}

	@Test
	public void testSimpleTransaction() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Worker> workers = new ArrayList<Worker>();
        Account[]accounts = Server.genAccounts();
        String[] transactions = new String[]{"A = B + C", "B = A + C"};
        
        for(String s : transactions) {
        	workers.add(new Worker(accounts, s));
        }
        
        Server.run(executor, workers.toArray(new Worker[1]));
        
        // It should either be: original B + original C or new B + original C
        Account A = accounts[0], B = accounts[1], C = accounts[2];
        assertTrue((getValue(A) == 24+23 && getValue(B) == (24+23)+23) || (getValue(A) == (25+23)+23 && getValue(B) == 25+23));
	}
	
	@Test
	public void testDifficultTransaction() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Worker> workers = new ArrayList<Worker>();
        Account[]accounts = Server.genAccounts();
        String[] transactions = new String[]{"A = B + C; B = A + C", "B = 0"};
        
        for(String s : transactions) {
        	workers.add(new Worker(accounts, s));
        }
        
        Server.run(executor, workers.toArray(new Worker[1]));
        
        // If the second gets executed first, A=C and B=C+C
        // If the first get's executed first, A=B+C, B=0
        Account A = accounts[0], B = accounts[1], C = accounts[2];
        assertTrue((getValue(A) == 23 && getValue(B) == 23+23) || (getValue(A) == 24+23 && getValue(B) == 0));
	}

}
