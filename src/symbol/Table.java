package symbol;

import java.util.*;

class SingleTable<T> {
    private SingleTable<T> parent;
    private HashMap<Symbol, T> table = new HashMap<Symbol, T>();
    private boolean mark;

    public SingleTable(SingleTable<T> parent, boolean mark) {
        this.parent = parent;
        this.mark = mark;
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

    public boolean isForeign(Symbol symbol, int state) {
        if (table.get(symbol) != null)
            return state == 1;
        if (parent == null)
            return false;
        if (state == 0 && mark == true)
            state = 1;
        return parent.isForeign(symbol, state);
    }

    public HashSet<Symbol> getLocals() {
        HashSet<Symbol> locals = new HashSet<Symbol>();
        for (Symbol s: table.keySet())
            locals.add(s);
        if (mark || parent == null)
            return locals;
        else {
            locals.addAll(parent.getLocals());
            return locals;
        }
    }

    public SingleTable<T> getParent() {
        return parent;
    }
}

public class Table<T> {
    SingleTable<T> head = new SingleTable<T>(null, false);
    
    public Table() {
    }

    public void put(Symbol symbol, T value) {
        head.put(symbol, value);
    }

    public T get(Symbol symbol) {
        return head.get(symbol);
    }

    public boolean isForeign(Symbol symbol) {
        return head.isForeign(symbol, 0);
    }

    public HashSet<Symbol> getLocals() {
        return head.getLocals();
    }

    public void beginScope() {
        beginScope(false);
    }

    public void beginScope(boolean mark) {
        SingleTable<T> t = new SingleTable<T>(head, mark);
        head = t;
    }

    public void endScope() {
        if (head.getParent() != null)
            head = head.getParent();
    }
}

