package tester;

import symbol.*;

public class SymbolTableTester {
    public static void main(String[] args) {
        Table<Integer> t = new Table<Integer>();
        t.put(Symbol.symbol("abc"), new Integer(1));
        t.put(Symbol.symbol("efg"), new Integer(2));
        System.out.println(t.get(Symbol.symbol("efg")));
        System.out.println(t.get(Symbol.symbol("")));
        t.put(Symbol.symbol("efg"), new Integer(3));
        System.out.println(t.get(Symbol.symbol("efg")));
        t.beginScope();
        System.out.println(t.get(Symbol.symbol("efg")));
        t.put(Symbol.symbol("efg"), new Integer(4));
        System.out.println(t.get(Symbol.symbol("efg")));
        t.put(Symbol.symbol("efg"), new Integer(5));
        System.out.println(t.get(Symbol.symbol("efg")));
        t.put(Symbol.symbol("new"), new Integer(6));
        System.out.println(t.get(Symbol.symbol("d")));
        System.out.println(t.get(Symbol.symbol("new")));
        t.endScope();
        System.out.println(t.get(Symbol.symbol("efg")));
        System.out.println(t.get(Symbol.symbol("new")));
    }
}

