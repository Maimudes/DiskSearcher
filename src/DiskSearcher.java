/*
 * By Noa Maimudes 207484494
 */

import java.io.*;

/*
 * Main application class.
 * This application searches for all files under some given path that contain
 * a given textual pattern. All files found are copied to some specific directory.
 */
public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;
    public static final int RESULTS_QUEUE_CAPACITY = 50;
    public static int id = 0;
    public DiskSearcher(){ }

    /*
    * Main method. Reads arguments from command line and starts the search.
    */
    public static void main(String[] args) {
        if(args.length != 6){
            System.out.println("Illegal number of arguments");
            return;
        }
        try{
            boolean isMilestones = Boolean.parseBoolean(args[0]);
            String extension = args[1];
            File root = new File(args[2]);
            File destination = new File(args[3]);
            int numOfSearchers = Integer.parseInt(args[4]);
            int numOfCopiers = Integer.parseInt(args[5]);

            if( numOfCopiers <= 0 || numOfSearchers <= 0 ){
                System.out.println("Invalid number copiers or searchers");
                return;
            }
            SynchronizedQueue mileStoneQueue = null;
            if (isMilestones){
                mileStoneQueue = new SynchronizedQueue<String>(DIRECTORY_QUEUE_CAPACITY + RESULTS_QUEUE_CAPACITY);
                mileStoneQueue.enqueue("“General, program has started the search”");
            }

            SynchronizedQueue directoryQueue = new SynchronizedQueue<File>(DIRECTORY_QUEUE_CAPACITY);
            SynchronizedQueue resultQueue = new SynchronizedQueue<File>(RESULTS_QUEUE_CAPACITY);

            // Scouter
            Thread scouter = new Thread(new Scouter(++id,directoryQueue, root, mileStoneQueue, isMilestones));
            scouter.run();

            // Searcher
            Thread[] searchers = new Thread[numOfSearchers];
            for (Thread searcher : searchers) {
                searcher = new Thread(new Searcher(++id, extension, directoryQueue, resultQueue, mileStoneQueue, isMilestones));
                searcher.run();
            }

            // Copier
            Thread[] copiers = new Thread[numOfCopiers];
                for (Thread copier : copiers) {
                    copier = new Thread(new Copier(id, destination, resultQueue, mileStoneQueue,isMilestones));
                    copier.run();
                }

        }catch(Exception e){
            e.getStackTrace();
        }

    }
}
