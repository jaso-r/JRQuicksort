package jrquicksort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A hastily-written multithreaded implementation of quicksort that
 * I wrote for funzies (really, just to make sure I still know how to do
 * things like this, as these kinds of skills are on my resume).
 * 
 * @author Jason Rickwald
 * @version 1.0
 * @param <T> A class for sorting lists of {@link java.lang.Comparable} objects.
 */
public class JRQuicksort <T extends Comparable<? super T>>
{
    /**
     * Helper class for returning the three "sides" of a quicksort step
     */
    private class JRSortLists
    {
        public ArrayList<T> leftList;
        public ArrayList<T> equalList;
        public ArrayList<T> rightList;
        
    }
    
    /**
     * Helper class that defines the quicksort operation to perform per thread
     */
    private class JRSortThread implements Callable<JRSortLists>
    {
        private final Comparable<? super T> pivot;
        private final List<T> sortList;
        private final int start;
        private final int end;
        
        /**
         * Constructor for the Callable "sort thread" object.
         * @param sortList The list to sort.
         * @param pivot The "pivot" object to compare to when making the left, right, and equals lists.
         * @param start Starting point of the list where sorting should begin (inclusive).
         * @param end End point of the list where sorting should end (exclusive).
         */
        public JRSortThread (List<T> sortList, Comparable<? super T> pivot, int start, int end)
        {
            this.sortList = sortList;
            this.pivot = pivot;
            this.start = start;
            this.end = end;
        }
        
        /**
         * The sorting operation to perform in a thread.
         * @return A JRSortThread object containing the three quicksort lists.
         */
        @Override
        public JRSortLists call ()
        {
            JRSortLists lists = new JRSortLists();
            lists.leftList = new ArrayList<>((end - start) + 1);
            lists.rightList = new ArrayList<>((end - start) + 1);
            lists.equalList = new ArrayList<>((end - start) + 1);
            for (Comparable<? super T> elem : sortList.subList(start, end))
            {
                if (elem.compareTo((T)pivot) < 0)
                    lists.leftList.add((T)elem);
                else if (elem.compareTo((T)pivot) > 0)
                    lists.rightList.add((T)elem);
                else
                    lists.equalList.add((T)elem);
            }
            return lists;
        }
    }
    
    /**
     * This value determines when to sort a list in the current thread, or
     * break up sorting into multiple threads.
     * This is fairly arbitrarily set to 1024 right now.
     * There is likely a better value for this that can be determined
     * by the program itself, or perhaps through an analysis of 
     * the efficiency/overhead of Java's concurrency library.
     */
    private static final int THREAD_CHUNK_SIZE = 1024;
    
    // A local reference to the list to be sorted
    private final List<T> sortList;
    
    // the ExecutorService used to run sorting threads
    static final ExecutorService service = Executors.newWorkStealingPool();
    
    /**
     * Constructor for a JRQuickSort object that can sort the given list.
     * @param list A list of {@link java.lang.Comparable} objects for sorting.
     */
    public JRQuicksort(List<T> list)
    {
        sortList = list;
    }
    
    /**
     * Run the sort operation in the list that was passed into the constructor.
     */
    public void sort ()
    {
        if (sortList == null || sortList.size() < 2)
        {
            return;
        }
        
        qsort(sortList);
    }
    
    /**
     * Private sort method that does most of the heavy lifting of the quicksort
     * algorithm, with some extra logic added in for sorting in multiple threads
     * if the provided list is large enough.
     * @param list The list to sort.
     */
    private void qsort (List<T> list)
    {
        // Error case or recursion termination case.
        if (list == null || list.size() < 2)
        {
            return;
        }
        
        // This is used a lot, so we're just making our own copy of the
        // size for simplicity.
        int listSize = list.size();
        
        // choosing a middle pivot for now
        Comparable<? super T> pivot = list.get(listSize / 2);
        
        // The three lists that this sort step will move objcts to
        ArrayList<T> leftList = new ArrayList<>(listSize);
        ArrayList<T> rightList = new ArrayList<>(listSize);
        ArrayList<T> equalList = new ArrayList<>(listSize);
        
        // Figure out if this list can be broken into multiple chunks to be
        // sorted concurrently.  Otherwise
        int chunkCount = (int) Math.ceil(((double)listSize) / ((double)THREAD_CHUNK_SIZE));
        if (chunkCount > 1)
        {
            int chunkLength = (int) Math.ceil(((double)listSize / ((double)chunkCount)));
            
            // make the thread objects to run and a list of Futures used
            // to join on to wait until they complete
            ArrayList<JRSortThread> threads = new ArrayList<>(chunkCount);
            for (int i = 0; i < chunkCount; i++)
            {
                int start = i * chunkLength;
                int end = Math.min((i + 1) * chunkLength, listSize);
                if (start <= end)
                {
                    JRSortThread st = new JRSortThread(list, pivot, start, end);
                    threads.add(i, st);
                }
            }
            List<Future<JRSortLists>> futures;
            
            try
            {
                // Run the threads using the work stealing pool
                futures = service.invokeAll(threads);
                
                // Iterate through the futures and get the results
                // Add the results to the main lists.
                for (Future<JRSortLists> f : futures)
                {
                    try
                    {
                        JRSortLists sl = f.get();
                        leftList.addAll(sl.leftList);
                        rightList.addAll(sl.rightList);
                        equalList.addAll(sl.equalList);
                    } catch (InterruptedException | ExecutionException ex)
                    {
                        Logger.getLogger(JRQuicksort.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (InterruptedException ex)
            {
                Logger.getLogger(JRQuicksort.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            // Go through the list and sort into the three lists on a single thread.
            for (Comparable<? super T> elem : list)
            {
                if (elem.compareTo((T)pivot) < 0)
                    leftList.add((T)elem);
                else if (elem.compareTo((T)pivot) > 0)
                    rightList.add((T)elem);
                else
                    equalList.add((T)elem);
            }
        }
        
        // Perform the recursive sort steps on the left and right lists
        // then concatinate the results to get a fully sorted list
        list.clear();
        qsort(leftList);
        qsort(rightList);
        list.addAll(leftList);
        list.addAll(equalList);
        list.addAll(rightList);
    }
}
