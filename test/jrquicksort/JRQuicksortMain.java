package jrquicksort;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * This is a driver application for testing my multithreaded quicksort.
 * @author Jason Rickwald
 * @version 1.0
 */
public class JRQuicksortMain
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Result result = JUnitCore.runClasses(JRQuicksortTest.class);
        for (Failure failure : result.getFailures())
        {
            System.out.println(failure.toString());
        }
    }
    
}
