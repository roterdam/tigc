package mips32;

import intermediate.*;
import java.util.*;
import regalloc.*;

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

class InstructionList implements Iterable<LabeledInstruction> {
    LabeledInstruction head, tail;

    public InstructionList() {
        head = null;
        tail = null;
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
}

