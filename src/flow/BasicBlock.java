package flow;

import java.util.*;
import intermediate.Label;
import intermediate.Temp;
import arch.Instruction;

public class BasicBlock {
    public LinkedList<Instruction> list = new LinkedList<Instruction>();
    public ArrayList<Label> labels = new ArrayList<Label>();

    private Set<Temp> uses = new HashSet<Temp>();
    private Set<Temp> defs = new HashSet<Temp>();

    public void add(Instruction ins) {
        list.add(ins);

        Set<Temp> t = new HashSet<Temp>(ins.use());
        t.removeAll(defs);
        uses.addAll(t);
        defs.addAll(ins.def());
    }

    public void add(Label label) {
        labels.add(label);
    }

    public boolean isEmpty() {
        if (list.isEmpty() && labels.isEmpty())
            return true;
        else
            return false;
    }

    public Set<Temp> use() {
        return uses;
    }

    public Set<Temp> def() {
        return defs;
    }
}

