package arch;

import intermediate.*;
import java.util.*;

public class StringTable {
    private HashMap<String, UnknownConstAccess> table = new HashMap<String, UnknownConstAccess>();
    private int count = 0;

    public StringTable() {
    }
    
    public UnknownConstAccess get(String s) {
        s = s.intern();
        if (table.containsKey(s))
            return table.get(s);
        else {
            UnknownConstAccess a = new UnknownConstAccess("S" + new Integer(count).toString());
            ++count;
            table.put(s, a);
            return a;
        }
    }
}

