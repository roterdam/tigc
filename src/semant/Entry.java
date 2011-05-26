package semant;

import intermediate.*;
import frame.*;

abstract class Entry {
}

class VarEntry extends Entry {
    type.Type type;
    boolean assignable;
    Temp place;

    public VarEntry(type.Type type, Temp place) {
        this(type, true, place);
    }

    public VarEntry(type.Type type, boolean assignable, Temp place) {
        this.type = type;
        this.assignable = assignable;
        this.place = place;
    }
}

class FuncEntry extends Entry {

    static class Invoking {
        symbol.Symbol name;
        java.util.HashSet<symbol.Symbol> locals;

        public Invoking(symbol.Symbol name, java.util.HashSet<symbol.Symbol> locals) {
            this.name = name;
            this.locals = locals;
        }
    }
    
    type.Record params;
    java.util.HashSet<symbol.Symbol> foreigns;
    java.util.ArrayList<Invoking> invokings;
    type.Type result;
    Label place = null;
    boolean isExtern; 
    Frame frame;

    public FuncEntry(type.Record params, type.Type result, Temp tResult, Label place,
            java.util.HashSet<symbol.Symbol> foreigns, java.util.ArrayList<Invoking> invokings,
            boolean isExtern) {
        this.params = params;
        this.result = result;
        this.foreigns = foreigns;
        this.invokings = invokings;
        this.place = place;
        this.isExtern = isExtern;
        frame = new Frame();
        frame.returnValue = tResult;
    }

    public FuncEntry(type.Record params, type.Type result, Temp tResult, Label place, boolean isExtern) {
        this(params, result, tResult, place,
                new java.util.HashSet<symbol.Symbol>(), new java.util.ArrayList<Invoking>(), isExtern);
    }
}

