/*
 * By Noa Maimudes 207484494
 */

import java.io.*;

/*
* A scouter thread This thread lists all sub-directories from a given root path.
* Each sub-directory is enqueued to be searched for files by Searcher threads.
*/
public class Scouter implements Runnable{

    private final int id;
    private File root;
    private SynchronizedQueue<File> directoryQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private Boolean isMilestones;
    private final Object lock;
    /*
    * Constructor.
    * Initializes the scouter with a queue for the directories to search,
    * and a starting root directory.
    */
    public Scouter(int id, SynchronizedQueue<File> directoryQueue, File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.directoryQueue = directoryQueue;
        this.root = root;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
        this.lock = new Object();
    }

    /*
    * override func. that runs the scouter thread.
    * Lists directories under root directory and adds them to queue,
    * then lists directories in the next level and enqueues them and so on.
    */
    @Override
    public void run() {
        // func. starts with registering to the directory queue as a producer
        this.directoryQueue.registerProducer();

        enqueueFile(this.root);

        // and when finishes, it unregisters from it.
        this.directoryQueue.unregisterProducer();
    }

    private void enqueueFile(File current){
        // Lists directories under root directory and adds them to queue
        // Checking for valid pathname
        try {
            FileFilter dirFilter = pathname -> pathname.isDirectory();
            File[] files = current.listFiles(dirFilter);

            // Then lists directories in the next level and enqueues them and so on.
            for (File file : files) {
                synchronized (lock){
                    if(isMilestones){
                        milestonesQueue.enqueue("Scouter on thread id "+id+": directory named "+file+" was scouted");
                    }
                    lock.notifyAll();
                }
                this.directoryQueue.enqueue(file);
                enqueueFile(file);
            }
        }catch(Exception e){
            e.getStackTrace();
        }
    }
}
