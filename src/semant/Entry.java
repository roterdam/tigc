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
    type.Record params;
    java.util.HashSet<symbol.Symbol> foreigns;
    java.util.HashSet<symbol.Symbol> invokings;
    type.Type result;
    Label place = null;
    java.util.LinkedList<Temp> formals = new java.util.LinkedList<Temp>();
    boolean isExtern; 

    public FuncEntry(type.Record params, type.Type result, Label place,
            java.util.HashSet<symbol.Symbol> foreigns, java.util.HashSet<symbol.Symbol> invokings,
            boolean isExtern) {
        this.params = params;
        this.result = result;
        this.foreigns = foreigns;
        this.invokings = invokings;
        this.place = place;
        this.isExtern = isExtern;
    }

    public FuncEntry(type.Record params, type.Type result, Label place, boolean isExtern) {
        this(params, result, place,
                new java.util.HashSet<symbol.Symbol>(), new java.util.HashSet<symbol.Symbol>(), isExtern);
    }
}

