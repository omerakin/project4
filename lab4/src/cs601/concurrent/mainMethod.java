package cs601.concurrent;

public class mainMethod {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
		rw.lockRead();
		System.out.println(rw.isReadLockHeldByCurrentThread());
		rw.unlockRead();
		System.out.println(rw.isReadLockHeldByCurrentThread());

	}

}
