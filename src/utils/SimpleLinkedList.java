package utils;

import java.util.Iterator;

class LinkedNode<T> {
    T data;
    LinkedNode<T> next;

    public LinkedNode(T x) {
        data = x;
        next = null;
    }
}

public class SimpleLinkedList<T> implements Iterable<T> {
    LinkedNode<T> head, tail;

    public SimpleLinkedList() {
        head = null;
        tail = null;
    }

    public void add(T x) {
        if (head == null) {
            head = new LinkedNode<T>(x);
            tail = head;
        } else {
            tail.next = new LinkedNode<T>(x);
            tail = tail.next;
        }
    }

    public static <T> SimpleLinkedList<T> merge(SimpleLinkedList<T> a, SimpleLinkedList<T> b) {
        if (a.head == null)
            return b;

        a.tail.next = b.head;
        if (b.tail != null)
            a.tail = b.tail;
        return a;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            LinkedNode<T> p = head;

            public boolean hasNext() {
                return p != null;
            }

            public T next() {
                T t = p.data;
                p = p.next;
                return t;
            }

            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }
}

