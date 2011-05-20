package semant;

abstract class Entry {
}

class VarEntry extends Entry {
    type.Type type;
    boolean assignable;

    public VarEntry(type.Type type) {
        this(type, true);
    }

    public VarEntry(type.Type type, boolean assignable) {
        this.type = type;
        this.assignable = assignable;
    }
}

class FuncEntry extends Entry {
    type.Record params;
    java.util.HashSet<symbol.Symbol> foreigns;
    java.util.HashSet<symbol.Symbol> invokings;
    type.Type result;

    public FuncEntry(type.Record params, type.Type result,
            java.util.HashSet<symbol.Symbol> foreigns, java.util.HashSet<symbol.Symbol> invokings) {
        this.params = params;
        this.result = result;
        this.foreigns = foreigns;
        this.invokings = invokings;
    }

    public FuncEntry(type.Record params, type.Type result) {
        this(params, result, new java.util.HashSet<symbol.Symbol>(), new java.util.HashSet<symbol.Symbol>());
    }
}


