package cs601.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * A reentrant read/write lock that allows: 
 * 1) Multiple readers (when there is no writer).
 * 2) One writer (when nobody else is writing or reading). 
 * 3) A writer is allowed to acquire a read lock while holding the write lock. 
 * The assignment is based on the assignment of Prof. Rollins (original author).
 */
public class ReentrantReadWriteLock {

	// TODO: Add instance variables : you need to keep track of the read lock holders and the write lock holders.
	// We should be able to find the number of read locks and the number of write locks 
	// a thread with the given threadId is holding 
	
	private Map<Long,Integer> readingThreads;
	private Map<Long,Integer> writingThread;
	
	/**
	 * Constructor for ReentrantReadWriteLock
	 */
	public ReentrantReadWriteLock() {
		// FILL IN CODE
		readingThreads = new HashMap<Long,Integer>();
		writingThread = new HashMap<Long,Integer>();
	}

	/**
	 * Returns true if the current thread holds a read lock.
	 * 
	 * @return
	 */
	public synchronized boolean isReadLockHeldByCurrentThread() {
		// FILL IN CODE
		return readingThreads.containsKey(Thread.currentThread().getId()); // don't forget to change it
	}

	/**
	 * Returns true if the current thread holds a write lock.
	 * 
	 * @return
	 */
	public synchronized boolean isWriteLockHeldByCurrentThread() {
		// FILL IN CODE
		return writingThread.containsKey(Thread.currentThread().getId()); // don't forget to change it
	}

	/**
	 * Non-blocking method that tries to acquire the read lock. Returns true
	 * if successful.
	 * 
	 * @return
	 */
	public synchronized boolean tryAcquiringReadLock() {
		// FILL IN CODE
		
		//Store the currentThread Id
		Long currentthread =Thread.currentThread().getId();
		//Check that current thread is same as writing thread
		if (writingThread.containsKey(currentthread)) {
			//if same,increase the number of this thread in readingThread maps and return true
			getSetReadingCount(currentthread);
			return true;
		} else if(writingThread.size() > 0) {
			// if current thread not same as writing thread and  
			// also if already writing from other thread return false
			return false;
		} else {
			// In other conditions, fill the readingThreads HashMap and return true
			getSetReadingCount(currentthread);
			return true;
		}
	}

	/**
	 * Non-blocking method that tries to acquire the write lock. Returns true
	 * if successful.
	 * 
	 * @return
	 */
	public synchronized boolean tryAcquiringWriteLock() {
		// FILL IN CODE
		
		//Store the currentThread Id
		Long currentthread = Thread.currentThread().getId();
		//Check that current thread is same as writing thread
		if (writingThread.containsKey(currentthread)) {
			//if same,increase the number of this thread in writingThread maps and return true
			getSetWritingCount(currentthread);
			return true;
		} else if (writingThread.size() > 0 || readingThreads.size() > 0) {
			// if current thread not same as writing thread and  
			// also if already reading from other thread return false
			return false;
		} else {
			// In other conditions, fill the writingThread HashMap and return true
			getSetWritingCount(currentthread);
			return true;
		}
	}

	/**
	 * Blocking method - calls tryAcquiringReadLock and returns only when the read lock has been
	 * acquired, otherwise waits.
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void lockRead() {
		// FILL IN CODE
		while(!tryAcquiringReadLock()){
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * @param currentthread Get the threadId of thread and 
	 * 			Checks that it is in the readingThreads HashMap
	 * 					If exist, then increase the count of this thread
	 * 					Else as a default, set it to 1
	 * 					Then put the readingThreads HashMap 
	 * @return null
	 */
	private Integer getSetReadingCount(Long currentthread) {
		// TODO Auto-generated method stub
		int count;
		if(readingThreads.containsKey(currentthread)){
			count = readingThreads.get(currentthread) + 1;
			readingThreads.remove(currentthread);
		} else {
			count = 1;
		}
		readingThreads.put(currentthread, count);
		return null;
	}

	/**
	 * Releases the read lock held by the current thread. 
	 */
	public synchronized void unlockRead() {
		// FILL IN CODE
		Long currentthread = Thread.currentThread().getId();
		if(readingThreads.get(currentthread) == 1){
			readingThreads.remove(currentthread);
		} else {
			readingThreads.put(currentthread, readingThreads.get(currentthread) - 1);
		}
		notifyAll();
	}

	/**
	 * Blocking method that calls tryAcquiringWriteLock and returns only when the write lock has been
	 * acquired, otherwise waits.
	 * 
	 * @throws InterruptedException
	 */
	public synchronized void lockWrite() {
		// FILL IN CODE
		while(!tryAcquiringWriteLock()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
	}

	/**
	 * @param currentthread Get the threadId of thread and 
	 * 			Checks that it is in the writingThread HashMap
	 * 					If exist, then increase the count of this thread
	 * 					Else as a default, set it to 1
	 * 					Then put the writingThread HashMap 
	 * @return
	 */
	private Integer getSetWritingCount(Long currentthread) {
		// TODO Auto-generated method stub
		int count;
		if(writingThread.containsKey(currentthread)){
			count = writingThread.get(currentthread) + 1;
			writingThread.remove(currentthread);
		} else {
			count = 1;
		}
		writingThread.put(currentthread, count);
		return null;
	}

	/**
	 * Releases the write lock held by the current thread. 
	 */
	public synchronized void unlockWrite() {
		// FILL IN CODE
		Long currentthread = Thread.currentThread().getId();
		if(writingThread.get(currentthread) == 1){
			writingThread.remove(currentthread);
		} else {
			writingThread.put(currentthread, writingThread.get(currentthread) - 1);
		}
		notifyAll();
	}
}
