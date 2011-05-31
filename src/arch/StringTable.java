package arch;

import intermediate.*;
import java.util.*;

public class StringTable implements Iterable<StringTable.StringPair> {
    private HashMap<String, UnknownConstAccess> table = new HashMap<String, UnknownConstAccess>();
    private int count = 0;

    public StringTable() {
    }
    
    public UnknownConstAccess get(String s) {
        s = s.intern();
        if (table.containsKey(s))
            return table.get(s);
        else {
            UnknownConstAccess a = new UnknownConstAccess("STRING" + new Integer(count).toString());
            ++count;
            table.put(s, a);
            return a;
        }
    }

    public static class StringPair {
        public String name;
        public String value;

        public StringPair(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public Iterator<StringPair> iterator() {
        return new Iterator<StringPair>() {
            Iterator<Map.Entry<String, UnknownConstAccess>> iter = table.entrySet().iterator();

            public boolean hasNext() {
                return iter.hasNext();
            }

            public StringPair next() {
                Map.Entry<String, UnknownConstAccess> next = iter.next();
                return new StringPair(next.getValue().name, next.getKey());
            }

            public void remove() {
                iter.remove();
            }
        };
    }
}

