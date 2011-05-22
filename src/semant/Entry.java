package semant;

import intermediate.*;

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

    static class Formal {
        symbol.Symbol name;
        type.Type type;
        Temp place;

        public Formal(symbol.Symbol name, type.Type type, Temp place) {
            this.name = name;
            this.type = type;
            this.place = place;
        }
    }

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
    Temp tResult;
    Label place = null;
    java.util.LinkedList<Formal> formals = new java.util.LinkedList<Formal>();
    boolean isExtern; 

    public FuncEntry(type.Record params, type.Type result, Temp tResult, Label place,
            java.util.HashSet<symbol.Symbol> foreigns, java.util.ArrayList<Invoking> invokings,
            boolean isExtern) {
        this.params = params;
        this.result = result;
        this.tResult = tResult;
        this.foreigns = foreigns;
        this.invokings = invokings;
        this.place = place;
        this.isExtern = isExtern;
    }

    public FuncEntry(type.Record params, type.Type result, Temp tResult, Label place, boolean isExtern) {
        this(params, result, tResult, place,
                new java.util.HashSet<symbol.Symbol>(), new java.util.ArrayList<Invoking>(), isExtern);
    }
}

