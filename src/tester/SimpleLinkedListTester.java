package tester;

import util.*;
import java.util.*;

public class SimpleLinkedListTester {
    public static void main(String[] args) {
        SimpleLinkedList<Integer> list1 = new SimpleLinkedList<Integer>(),
            list2 = new SimpleLinkedList<Integer>();
        LinkedList<Integer> list3 = new LinkedList<Integer>(),
            list4 = new LinkedList<Integer>();
        for (int i = 0; i < 1000000; ++i) {
            //list1.add(new Integer(i));
            //list2.add(new Integer(i));
            list3.add(new Integer(i));
            list4.add(new Integer(i));
        }
        //list1.addAll(list2);
        list3.addAll(list4);
    }
}

