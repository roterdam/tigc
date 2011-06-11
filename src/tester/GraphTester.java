package tester;

import util.*;
import java.util.*;

public class GraphTester {
    public static void main(String[] args) {
        Graph<Integer> g = new Graph<Integer>();
        g.addEdge(new Integer(1), new Integer(2));
        g.addEdge(new Integer(1), new Integer(3));
        g.addEdge(new Integer(3), new Integer(5));
        g.addEdge(new Integer(4), new Integer(5));
        g.addEdge(new Integer(4), new Integer(2));

        for (Integer i: g.topologicalSort()) {
            System.out.println(i.toString());
        }
    }
}

