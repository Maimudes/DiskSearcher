/*
 * By Noa Maimudes 207484494
 */

import java.io.*;
/*
 * this class reads a file from the results queue (see 'Searcher' class) and copies it into a specified destination directory
 */

public class Copier implements Runnable{
	public static final int COPY_BUFFER_SIZE = 4096;
	private int id;
	private File destination;
	private SynchronizedQueue<File> resultsQueue;
	private SynchronizedQueue<String> milestonesQueue;
	private boolean isMilestones;
	private final Object lock;

	/*
	 * Constructor.
	 * Initializes the Copier that will read files from 'results queue'
	 * and copies them to 'destination'
	 */
	Copier(int id, File destination, SynchronizedQueue <File> resultsQueue, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){

		this.id = id;
		this.resultsQueue = resultsQueue;
		this.destination = destination;
		this.milestonesQueue = milestonesQueue;
		this.lock = new Object();
	}
	/*
	 * override func. that runs the copier thread.
	 * Reads from results queue (by buffer) and than copies to destination directory.
	 */
	public void run (){

		try{
			if (!destination.exists())
			{
				destination.mkdirs();
			}
			File nextFile; //next file in queue
			while ((nextFile = resultsQueue.dequeue()) != null){
			
				InputStream in = new FileInputStream(nextFile.getPath());
				OutputStream out = new FileOutputStream(this.destination.getPath() + File.separator + nextFile.getName());
				byte[] buffer = new byte[COPY_BUFFER_SIZE];
				int temp;
				while((temp = in.read(buffer)) > 0)
				{
					out.write(buffer,0,temp);
				}
				in.close();
				out.close();
				synchronized (lock){
					if(isMilestones){
						milestonesQueue.enqueue("Copier from thread id "+id+": file named "+nextFile+" was copied");
					}
					lock.notifyAll();
				}
			}
		}
			catch(IOException e)
			{
				System.out.println(e);
			}
		
	}
}
