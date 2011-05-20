package tester;

import utils.SimpleLinkedList;

public class SimpleLinkedListTester {
    public static void main(String[] args) {
        SimpleLinkedList<Integer> list1 = new SimpleLinkedList<Integer>(),
            list2 = new SimpleLinkedList<Integer>();
        for (int i = 0; i < 10; ++i)
            list1.add(new Integer(i));
        for (int i = 10; i < 20; ++i)
            list2.add(new Integer(i));
        SimpleLinkedList<Integer> list3 = SimpleLinkedList.merge(list1, list2);
        for (Integer x: list3)
            System.out.println(x.toString());
    }
}

