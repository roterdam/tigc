package semant;

import intermediate.*;
import frame.*;

abstract class Entry {
}

class VarEntry extends Entry {
    type.Type type;
    boolean assignable;
    Temp place;
    Integer c = null;

    public VarEntry(type.Type type, Temp place, Integer c) {
        this(type, true, place);
        this.c = c;
    }

    public VarEntry(type.Type type, boolean assignable, Temp place) {
        this.type = type;
        this.assignable = assignable;
        this.place = place;
    }
}

class FuncEntry extends Entry {

    type.Record params;
    type.Type result;
    boolean isExtern; 
    Frame frame;

    public FuncEntry(type.Record params, type.Type result, Frame frame, boolean isExtern) {
        this.params = params;
        this.result = result;
        this.isExtern = isExtern;
        this.frame = frame;
    }
}

