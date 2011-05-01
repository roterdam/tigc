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
    type.Type result;

    public FuncEntry(type.Record params, type.Type result) {
        this.params = params;
        this.result = result;
    }
}


