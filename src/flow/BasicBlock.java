package flow;

import java.util.*;
import intermediate.Label;
import intermediate.Temp;
import arch.Instruction;

public class BasicBlock implements Iterable<Instruction> {
    public ArrayList<Label> labels = new ArrayList<Label>();
    private LinkedList<Instruction> list = new LinkedList<Instruction>();
    private Map<Instruction, Integer> order = new HashMap<Instruction, Integer>();
    private int n = 0;

    private Set<Temp> uses = new HashSet<Temp>();
    private Set<Temp> defs = new HashSet<Temp>();

    public void add(Instruction ins) {
        list.add(ins);
        order.put(ins, new Integer(n++));

        Set<Temp> t = new HashSet<Temp>(ins.use());
        t.removeAll(defs);
        uses.addAll(t);
        defs.addAll(ins.def());
    }

    public boolean before(Instruction a, Instruction b) {
        Integer x = order.get(a), y = order.get(b);
        if (a == null || b == null)
            return false;
        return x.intValue() < y.intValue();
    }

    public void replace(BasicBlock block) {
        replace(block.list);
    }

    private void replace(List<Instruction> insList) {
        list = new LinkedList<Instruction>();
        uses = new HashSet<Temp>();
        defs = new HashSet<Temp>();
        order = new HashMap<Instruction, Integer>();
        n = 0;
        for (Instruction i: insList)
            add(i);
    }

    public void removeInstruction(Instruction ins) {
        List<Instruction> newList = new LinkedList<Instruction>(list);
        newList.remove(ins);
        replace(newList);
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

    public Iterator<Instruction> iterator() {
        return list.iterator();
    }

    public Iterator<Instruction> descendingIterator() {
        return list.descendingIterator();
    }

    public boolean isInsEmpty() {
        return list.isEmpty();
    }

    public Instruction getLast() {
        return list.getLast();
    }
}

