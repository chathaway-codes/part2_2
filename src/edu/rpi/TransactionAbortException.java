package edu.rpi;

public class TransactionAbortException extends Exception {
	public long delay;
	TransactionAbortException(long delay) {
		this.delay = delay;
	}
}
