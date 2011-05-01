package symbol;

import java.util.*;

public class Symbol {
    private String name;
    private static HashMap<String, Symbol> map = new HashMap<String, Symbol>();

    private Symbol(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }


    public static Symbol symbol(String name) {
        String u = name.intern();
        Symbol s = map.get(u);
        if (s == null) {
            s = new Symbol(u);
            map.put(u, s);
        }
        return s;
    }
}

