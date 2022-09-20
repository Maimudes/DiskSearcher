/* By Noa Maimudes 207484494
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 * 
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

	private T[] buffer;
	private int producers;
	// TODO: Add more private members here as necessary
	private final Object lock;
	private int firstIndex;
	private int lastIndex;
	private int size;
	private int capacity;

	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * @param capacity Buffer capacity
	 */

	public SynchronizedQueue(int capacity) {
		this.buffer = (T[])(new Object[capacity]);
		this.producers = 0;
		// TODO: Add more logic here as necessary
		this.firstIndex = 0;
		this.lastIndex = 0;
		this.size = 0;
		this.capacity = capacity;
		this.lock = new Object();
	}
	
	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue, 
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this 
	 * queue (because no producers are registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public T dequeue() {

		synchronized(lock) {
			while(this.size == 0) {
				//free lock and returns null in case the queue is empty and no producers
				if (this.producers == 0) {
					lock.notifyAll();
					return null;
				} 
				// there are producers - block until item is available
					try {
						lock.wait();	
					}
					catch(InterruptedException e ) {
						System.out.println(e);
						return null;
					}
			}
			// the queue isn't empty - returning item and advancing the queue
			if (this.lastIndex == this.buffer.length) {
				this.lastIndex = 0;
			}
			T item = this.buffer[this.lastIndex];
			this.lastIndex++;
			this.size--;
			lock.notifyAll();
			return item;
		}
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this 
	 * method blocks until some space becomes available.
	 * 
	 * @param item Item to enqueue
	 */
	public void enqueue(T item) {
		synchronized(lock){
			while (this.size == this.buffer.length) {
				try {
						lock.wait();	
				}
				catch(InterruptedException e ) {
					System.out.println(e);
					return;
				}
			}
			if (this.firstIndex == this.buffer.length) {
				this.firstIndex = 0 ;
			}
			this.buffer[this.firstIndex] = item;
			this.firstIndex++;
			this.size++;
			lock.notifyAll();
		}
	}

	/**
	 * Returns the capacity of this queue
	 * @return queue capacity
	 */
	public int getCapacity() {
		return this.capacity;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * @return queue size
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to 
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
	 * finishes to enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public void registerProducer() {
		// TODO: This should be in a critical section
		synchronized(lock) {
			this.producers++;
			lock.notifyAll();
		}
	}

	/**
	 * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public void unregisterProducer() {
		// TODO: This should be in a critical section
		synchronized(lock) {
		this.producers--;
		lock.notifyAll();
		}
	}
}
