package arch;

import java.util.*;
import intermediate.Temp;
import frame.Frame;

public abstract class Instruction {

    public Frame frame;

    public abstract List<Temp> useList();
    public Set<Temp> use() {
        return new HashSet<Temp>(useList());
    }
    public abstract Set<Temp> def();

    public abstract boolean hasSideEffects();
    public abstract boolean isLoad();
    public abstract boolean isStore();
    public abstract Instruction rewrite(List<Temp> params);

    public abstract int opCode();

    public abstract boolean isJump();
}

