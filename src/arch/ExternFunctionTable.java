package arch;

import symbol.Symbol;
import intermediate.*;
import java.util.*;

public class ExternFunctionTable {
    private HashSet<Symbol> table = new HashSet<Symbol>();
    private int count = 0;

    public ExternFunctionTable() {
    }
    
    public void put(Symbol s) {
        table.add(s);
    }
}

