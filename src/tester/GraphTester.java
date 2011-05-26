package tester;

import util.*;
import java.util.*;

public class GraphTester {
    public static void main(String[] args) {
        Graph<Integer> g = new Graph<Integer>();
        g.addEdge(new Integer(1), new Integer(2));
        g.addEdge(new Integer(2), new Integer(3));
        g.addEdge(new Integer(2), new Integer(4));
        g.addEdge(new Integer(3), new Integer(1));

        System.out.println("Successor of 1:");
        for (Integer i: g.succ(new Integer(1)))
            System.out.println(i.toString());
        System.out.println("Predesessor of 1:");
        for (Integer i: g.pred(new Integer(1)))
            System.out.println(i.toString());
        System.out.println(new Boolean(g.isLoopEdge(new Integer(2), new Integer(4))).toString());
        g.addEdge(new Integer(4), new Integer(3));
        System.out.println(new Boolean(g.isLoopEdge(new Integer(2), new Integer(4))).toString());
    }
}

