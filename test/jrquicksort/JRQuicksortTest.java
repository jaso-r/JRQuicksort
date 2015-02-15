package jrquicksort;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A JUnit test class for testing the JRQuicksort.
 * @author Jason
 */
public class JRQuicksortTest
{
    private static final int INT_LIST_SIZE = 20224563;
    private final ArrayList<Integer> intList;
    
    public JRQuicksortTest()
    {
        intList = new ArrayList<>(INT_LIST_SIZE);
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
        System.out.println("Building a large list of ints to sort.");
        System.out.println("(this may take a little while)");
        // fill the list of Ints
        for (int i = 0; i < INT_LIST_SIZE; i++)
        {
            intList.add(i, (int) (Math.random() * 1000000.0));
        }
    }
    
    @After
    public void tearDown()
    {
        intList.clear();
    }

    /**
     * Test of sort method, of class JRQuicksort.
     */
    @Test
    public void testSort()
    {
        System.out.println("Testing sort...");
        
        // start with some really simple test cases
        String[] singleString = {"Foo"};
        String[] twoStrings = {"Beta", "Echo"};
        String[] threeStrings = {"Romeo", "Alpha", "Charlie"};
        String[] manyStrings = {"Gamma", "Alpha", "Tango", "Foxtrot", "Foxtrot",
            "Beta", "Romeo", "Juliet", "Echo", "Echo", "Echo", "Mountain", 
            "Echo", "Alpha", "Washington", "Zebra", "Yellow", "Beta", "Nickle",
            "Voronoi", "Classify", "Kilo", "Lamb", "Understanding", "Welcome"};
        
        // first sort an empty list
        System.out.println("Empty list");
        ArrayList<String> stringList = new ArrayList<>();
        JRQuicksort instance = new JRQuicksort(stringList);
        int listSize = 0;
        instance.sort();
        assertTrue("Empty list size changed", stringList.size() == listSize);
        
        // now sort a list with a single item
        System.out.println("Single string list");
        for (String elem : singleString)
            stringList.add(elem);
        listSize = stringList.size();
        instance.sort();
        assertTrue("Single string list size changed", stringList.size() == listSize);
        
        // sort two items
        stringList.clear();
        System.out.println("Two string list");
        for (String elem : twoStrings)
            stringList.add(elem);
        listSize = stringList.size();
        instance.sort();
        assertTrue("Two strings list size changed", stringList.size() == listSize);
        assertTrue("Two strings list out of order", checkOrder(stringList));
        
        // sort three items
        System.out.println("Three string list");
        stringList.clear();
        for (String elem : threeStrings)
            stringList.add(elem);
        listSize = stringList.size();
        instance.sort();
        assertTrue("Three strings list size changed", stringList.size() == listSize);
        assertTrue("Three strings list out of order", checkOrder(stringList));
        
        // sort many items
        System.out.println("Many string list");
        stringList.clear();
        for (String elem : manyStrings)
            stringList.add(elem);
        listSize = stringList.size();
        instance.sort();
        assertTrue("Many strings list size changed", stringList.size() == listSize);
        assertTrue("Many strings list out of order", checkOrder(stringList));
        
        // sort a very large list of items
        // enough to trigger the use of multple threads
        System.out.println("Very large list of Integers");
        listSize = intList.size();
        instance = new JRQuicksort(intList);
        instance.sort();
        assertTrue("Int list size changed", intList.size() == listSize);
        assertTrue("Int list out of order", checkOrder(stringList));
    }
    
    /**
     * A function used to check that a given list is properly sorted.
     * @param <T> The type of Comparable that the list contains.
     * @param list A list to iterate through and check order.
     * @return true if the list is in order, false if it is not.
     */
    private static <T extends Comparable<? super T>> boolean checkOrder(List<T> list)
    {
        Comparable lastElem = null;
        for (Comparable listElem : list)
        {
            if (lastElem != null)
            {
                if (lastElem.compareTo(listElem) > 0)
                {
                    return false;
                }
            }
            lastElem = listElem;
        }
        return true;
    }
    
}
