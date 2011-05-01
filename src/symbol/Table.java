package symbol;

import java.util.*;

class SingleTable<T> {
    private SingleTable<T> parent;
    private HashMap<Symbol, T> table = new HashMap<Symbol, T>();

    public SingleTable(SingleTable<T> parent) {
        this.parent = parent;
    }

    public void put(Symbol symbol, T value) {
        table.put(symbol, value);
    }

    public T get(Symbol symbol) {
        T v = table.get(symbol);
        if (v != null)
            return v;
        else if (parent != null)
            return parent.get(symbol);
        else
            return null;
    }

    public SingleTable<T> getParent() {
        return parent;
    }
}

public class Table<T> {
    SingleTable<T> head = new SingleTable<T>(null);
    
    public Table() {
    }

    public void put(Symbol symbol, T value) {
        head.put(symbol, value);
    }

    public T get(Symbol symbol) {
        return head.get(symbol);
    }

    public void beginScope() {
        SingleTable<T> t = new SingleTable<T>(head);
        head = t;
    }

    public void endScope() /*throws Exception*/ {
        if (head.getParent() != null)
            head = head.getParent();
//        else
//            throw new Exception("Scope doesn't match");
    }
}

