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

public class PointerTest {
	
	public static Integer getValue(Account account) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	Field f = account.getClass().getDeclaredField("value");
    	f.setAccessible(true);
    	Object _value = f.get(account);
    	Integer value = (Integer)_value;
    	return value;
	}
	
	@Test
	public void testSimplePointer() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Worker> workers = new ArrayList<Worker>();
        Account[]accounts = Server.genAccounts();
        String[] transactions = new String[]{"A = 2; B = A*"};
        
        for(String s : transactions) {
        	workers.add(new Worker(accounts, s));
        }
        
        Server.run(executor, workers.toArray(new Worker[1]));
        
        // A->C = 23
        Account A = accounts[0], B = accounts[1], C = accounts[2];
        assertEquals(new Integer(23), getValue(B));
	}
	
	@Test
	public void testProfExample() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Worker> workers = new ArrayList<Worker>();
        Account[]accounts = Server.genAccounts();
        String[] transactions = new String[]{"A = Q**; B = A + A*"};
        
        for(String s : transactions) {
        	workers.add(new Worker(accounts, s));
        }
        
        Server.run(executor, workers.toArray(new Worker[1]));
        
        // A->C = 23
        Account A = accounts[0], B = accounts[1], C = accounts[2];
        assertEquals(new Integer(9+16), getValue(B));
	}
}
