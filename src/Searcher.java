/*
 * By Noa Maimudes 207484494
 */

import java.io.*;

/*
* A searcher thread. Searches for files containing a given pattern and that end with a specific
* extension in all directories listed in a directory queue.
*/
public class Searcher implements Runnable{

    private int id;
    private String extension;
    private SynchronizedQueue<File> directoryQueue;
    private SynchronizedQueue<File> resultsQueue;
    private SynchronizedQueue<String> milestonesQueue;
    private Boolean isMilestones;
    private final Object lock;

    /*
    * Constructor.
    * Initializes the Searcher with a queues for the directories and results.
    */
    public Searcher(int id, String extension ,SynchronizedQueue<File> directoryQueue,
                    SynchronizedQueue<File> resultsQueue, SynchronizedQueue<String> milestonesQueue, Boolean isMilestones){
        this.id = id;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
        this.milestonesQueue = milestonesQueue;
        this.isMilestones = isMilestones;
        this.lock = new Object();
    }

    /*
    * override func. that runs the searcher thread.
    * Thread will fetch a directory to search in from the directory queue,
    * and search all files inside it (without recursion)
    * Files that contains the pattern and have the wanted extension will be enqueued to the results queue.
    */
    @Override
    public void run(){
        this.resultsQueue.registerProducer();
        File directory;

        while ((directory = this.directoryQueue.dequeue()) != null){
            // Files that a contain the pattern and have the wanted extension
            FileFilter isFile = pathname -> pathname.isFile()
                    && pathname.getName().endsWith(this.extension);
            // Enqueued to the results queue
            File[] files = directory.listFiles(isFile);
            for (File file : files){
                synchronized (lock){
                    if(isMilestones){
                        milestonesQueue.enqueue("Searcher on thread id "+id+": file named "+file+" was found");
                    }
                    lock.notifyAll();
                }
                this.resultsQueue.enqueue(file);
            }
        }
        this.resultsQueue.unregisterProducer();
    }
}
