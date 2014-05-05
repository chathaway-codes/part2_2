package edu.rpi.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import edu.rpi.Account;
import edu.rpi.Server;
import edu.rpi.Worker;

public class IncrementTest {

	@Test
	public void test() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		// Open the test file
        String line;
        BufferedReader input =
            new BufferedReader(new FileReader("increment.txt"));
        ExecutorService executor = Executors.newCachedThreadPool();
        ArrayList<Worker> workers = new ArrayList<Worker>();
        Account[]accounts = Server.genAccounts();

        while ((line = input.readLine()) != null) {
        	workers.add(new Worker(accounts, line));
        }
        
        input.close();
        
        Server.run(executor, workers.toArray(new Worker[1]));
        
         Integer[] results = new Integer[]{26,25,24,23,22,21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2,1};
        
        // Verify the result values in all the classes
         int i = 0;
        for(Account account : accounts) {
        	Field f = account.getClass().getDeclaredField("value");
        	f.setAccessible(true);
        	Object _value = f.get(account);
        	Integer value = (Integer)_value;
        	assertEquals(value, results[i++]);
        }
	}

}
