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
    public abstract boolean isJump();
    public abstract boolean isLoad();
    public abstract boolean isStore();
    // if isMove() returns true, then useList().get(0) must be the move source
    public abstract boolean isMove();
    public abstract boolean sameExceptTemps(Instruction i);

    public abstract Instruction rewrite(Temp dst, List<Temp> params);
    public abstract Instruction rewriteMove(Temp src);

    public abstract int opCode();

    public abstract String toString();
    public abstract String toString(Map tempNameMap);
}

