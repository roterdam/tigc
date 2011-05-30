package util;

import java.util.*;

class SimpleLinkedNode<T> {
    T data;
    SimpleLinkedNode<T> next;

    public SimpleLinkedNode(T data, SimpleLinkedNode<T> next) {
        this.data = data;
        this.next = next;
    }
}

public class SimpleLinkedList<T> implements Iterable<T> {
    SimpleLinkedNode<T> head, tail;

    public SimpleLinkedList() {
        head = null;
        tail = null;
    }

    public void add(T x) {
        if (head == null) {
            head = new SimpleLinkedNode<T>(x, null);
            tail = head;
        } else {
            tail.next = new SimpleLinkedNode<T>(x, null);
            tail = tail.next;
        }
    }

    public void addFirst(T x) {
        head = new SimpleLinkedNode<T>(x, head);
        if (tail == null)
            tail = head;
    }

    public void addAll(SimpleLinkedList<T> list) {
        if (head == null) {
            head = list.head;
            tail = list.tail;
        } else if (list.head != null) {
            tail.next = list.head;
            tail = list.tail;
        }
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            SimpleLinkedNode<T> p = head;

            public boolean hasNext() {
                return p != null;
            }

            public T next() {
                T ret = p.data;
                p = p.next;
                return ret;
            }

            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }
}

