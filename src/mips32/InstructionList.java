package mips32;

import intermediate.*;
import java.util.*;

class LabeledInstruction {
    Label label;
    Instruction instruction;
    LabeledInstruction next;

    public LabeledInstruction(Label label, Instruction instruction, LabeledInstruction next) {
        this.label = label;
        this.instruction = instruction;
        this.next = next;
    }

    public String toString(Map map) {
        String s = "";
        if (label != null)
            s += label.toString() + ":";
        if (instruction != null) {
            if (map == null)
                s += "\t" + instruction.toString();
            else
                s += "\t" + instruction.toString(map);
        }
        return s;
    }

    public String toString() {
        return toString(null);
    }
}

class InstructionList extends arch.InstructionList implements Iterable<LabeledInstruction> {
    LabeledInstruction head, tail;

    public InstructionList() {
        head = null;
        tail = null;
    }

    public LabeledInstruction add(LabeledInstruction ins) {
        return add(ins.label, ins.instruction);
    }

    public LabeledInstruction add(Label label, Instruction ins) {
        LabeledInstruction r = new LabeledInstruction(label, ins, null);
        if (head == null) {
            head = r;
            tail = head;
        } else {
            tail.next = r;
            tail = tail.next;
        }
        return r;
    }

    public LabeledInstruction add(Instruction ins) {
        return add(null, ins);
    }

    public LabeledInstruction add(Label label) {
        return add(label, null);
    }

    public void addAllBefore(List<Label> frontLabels, List<arch.Instruction> list, Label beforeLabel) {
        InstructionList ret = new InstructionList();

        boolean added = false;
        for (LabeledInstruction i: this) {
            if (i.label == beforeLabel && !added) {
                for (Label l: frontLabels)
                    ret.add(l);
                for (arch.Instruction j: list)
                    if (j instanceof Instruction)
                        ret.add((Instruction) j);
                added = true;
            }
            ret.add(i);
        }

        head = ret.head;
        tail = ret.tail;
    }

    public void replaceLabel(List<Label> oldLabels, Label newLabel) {
        InstructionList ret = new InstructionList();

        boolean replaced = false;
        for (LabeledInstruction li: this) {
            if (li.label != null && oldLabels.contains(li.label)) {
                if (!replaced) {
                    replaced = true;
                    ret.add(newLabel);
                }
            } else
                ret.add(li);
        }

        head = ret.head;
        tail = ret.tail;
    }

    public LabeledInstruction addPlaceHolder() {
        return add(null, null);
    }

    public Iterator<LabeledInstruction> iterator() {
        return new Iterator<LabeledInstruction>() {
            LabeledInstruction p = head;

            public boolean hasNext() {
                return p != null;
            }

            public LabeledInstruction next() {
                LabeledInstruction t = p;
                p = p.next;
                return t;
            }

            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String toString() {
        String ret = "";
        for (LabeledInstruction li: this)
            ret += li.toString() + "\n";
        return ret;
    }

    private LabeledInstruction find(Instruction ins) {
        LabeledInstruction p = head;
        while (p != null) {
            if (p.instruction != null && p.instruction == ins)
                return p;
            p = p.next;
        }
        return null;
    }
    
    public void redirect(arch.Instruction ins, List<Label> oldPlace, Label newPlace) {
        if (!(ins instanceof Instruction))
            return;

        LabeledInstruction o = find((Instruction) ins);
        if (o == null)
            return;

        if (oldPlace.contains(o.instruction.target))
            o.instruction.target = newPlace;
        else {
            Instruction j = Instruction.J(o.instruction.frame, newPlace);
            o.next = new LabeledInstruction(null, j, o.next);
        }
    }
}

