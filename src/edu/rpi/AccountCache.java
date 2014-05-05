package edu.rpi;

public class AccountCache implements Comparable {
	protected Account account;
	protected Integer o_value;
	public String action;
	public Integer order;
	private Integer value;
	private boolean requires_write = false;
	
	public AccountCache(Account account, int order) {
		this.account = account;
		this.action = "N";
		this.order = order;
	}
	
	public void verify() throws TransactionAbortException {
		if(account == null)
			return;
		// If there is no o_value, we never read it... So we can just override it
		if(this.o_value != null)
			account.verify(this.o_value);
	}
	
	public void open() throws TransactionAbortException {
		if(account == null)
			return;
		account.open(false);
		this.verify();
		if(requires_write) {
			account.open(true);
		}
	}
	
	public void setValue(int value) {
		this.value = value;
		this.requires_write = true;
		this.action = "Y";
	}
	
	public int getValue() {
		if(this.value == null) {
			this.value = account.peek();
			this.o_value = value;
			this.action = "Y";
		}
		return this.value;
	}
	
	public void close() {
		if(account == null)
			return;
		account.close();
	}
	
	public void commit() throws TransactionAbortException {
		if(account == null)
			return;
		if(this.requires_write) {
			account.update(value);
		}
	}

	@Override
	public int compareTo(Object arg0) {
		if(!(arg0 instanceof AccountCache))
			return 0;
		AccountCache other = (AccountCache)arg0;
		if(other.account == null || this.account == null)
			return 0;
		int result = this.order.compareTo(other.order);
		if(result == 0) {
			if(this.action.equals("="))
				return -1;
			return 1;
		}
		return result;
	}
}
