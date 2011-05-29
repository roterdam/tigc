package arch;

import java.util.*;
import intermediate.Temp;
import frame.Frame;

public abstract class Instruction {

    public Frame frame;

    public abstract Set<Temp> use();
    public abstract Set<Temp> def();

    public abstract boolean isJump();
}

