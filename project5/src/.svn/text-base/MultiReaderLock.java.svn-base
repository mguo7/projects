/**
 * A simple custom lock that allows simultaneously read operations, but
 * disallows simultaneously write and read/write operations.
 * 
 * You do not need to implement any form or priority to read or write
 * operations. The first thread that acquires the appropriate lock should be
 * allowed to continue.
 * 
 * @author CS 212 Software Development
 * @author University of San Francisco
 */
public class MultiReaderLock {

	/**
	 * Initializes a multi-reader (single-writer) lock.
	 */
	private int readers;
	private int writers;

	public MultiReaderLock() {

		readers = 0;
		writers = 0;
	}

	/**
	 * Will wait until there are no active writers in the system, and then will
	 * increase the number of active readers.
	 */
	public synchronized void lockRead() {
		while (writers > 0) {

			try {
				this.wait();
			} catch (InterruptedException e) {
				System.err.println("Do not read. Writers are working!");
			}
		}

		readers++;
	}

	/**
	 * Will decrease the number of active readers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockRead() {
		if (readers > 0) {

			readers--;

		}
		this.notifyAll();
	}

	/**
	 * Will wait until there are no active readers or writers in the system, and
	 * then will increase the number of active writers.
	 */
	public synchronized void lockWrite() {
		while (readers > 0 || writers > 0) {

			try {
				this.wait();
			} catch (InterruptedException e) {
				System.err
						.println("Readers and Writers are working at the same time!");
			}
		}

		writers++;
	}

	/**
	 * Will decrease the number of active writers, and notify any waiting
	 * threads if necessary.
	 */
	public synchronized void unlockWrite() {

		if (writers > 0) {

			writers--;
		}
		this.notifyAll();
	}
}