package cs601.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * A test class for the ReentrantReadWriteLock.
 * Modified from the tests of Prof. Rollins 
 * Original author: Prof. Rollins
 */
public class ReentrantReadWriteLockTest {

	@Test
	public void testLockSimple() {		
		String testName = "testLockSimple";
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		lock.lockWrite();
		lock.lockRead();

		Assert.assertTrue(String.format("%n" + "Test Case: %s%n" +
				" Read lock not held. %n", testName), lock.isReadLockHeldByCurrentThread());
		lock.unlockRead();
		Assert.assertFalse(String.format("%n" + "Test Case: %s%n" +
				" Read lock not released. %n", testName), lock.isReadLockHeldByCurrentThread());
		Assert.assertTrue(String.format("%n" + "Test Case: %s%n" +
				" Write lock not held. %n", testName), lock.isWriteLockHeldByCurrentThread());
		lock.unlockWrite();
		Assert.assertFalse(String.format("%n" + "Test Case: %s%n" +
				" Write lock not released. %n", testName), lock.isWriteLockHeldByCurrentThread());
	}

	@Test
	public void testLockMultipleWrites() {		
		String testName = "testLockMultipleWrites";
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		lock.lockWrite();
		lock.lockWrite();

		lock.unlockWrite();
		Assert.assertTrue(String.format("%n" + "Test Case: %s%n" +
				" Write lock not held. %n", testName), lock.isWriteLockHeldByCurrentThread());
		lock.unlockWrite();
	}

	@Test
	public void testWriteLockMultiThread() {	
		String testName = "testWriteLockMultiThread";

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		boolean result = lock.tryAcquiringWriteLock();
		if(!result) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" +
					" Unable to acquire write lock. %n", testName));
		}
		Thread t1 = new Thread() {
			public void run() {
				Assert.assertFalse(String.format("%n" + "Test Case: %s%n" +
						" Read lock acquired. %n", testName), lock.tryAcquiringReadLock());
			}
		};
		
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			Assert.fail();
		}
		lock.unlockWrite();
	}

	@Test
	public void testReadLockMultiThread() {	
		String testName = "testReadLockMultiThread";

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		lock.lockRead();

		Thread t1 = new Thread() {
			public void run() {
				Assert.assertTrue(String.format("%n" + "Test Case: %s%n" +
						" Read lock not acquired. %n", testName), lock.tryAcquiringReadLock());
			}
		};

		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			Assert.fail();
		}
		lock.unlockRead();
	}

	@Test
	public void testLockUpgrade() {
		String testName = "testLockUpgrade";

		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		lock.lockRead();

		boolean result = lock.tryAcquiringWriteLock();
		if(result) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" +
					" Lock upgrade read to write should be disallowed. %n", testName));
		}
		lock.unlockRead();

	}

	
	
}
