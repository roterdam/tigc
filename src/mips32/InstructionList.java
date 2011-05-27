package mips32;

import util.*;
import intermediate.*;

class LabeledInstruction {
    Label label;
    Instruction instruction;

    public LabeledInstruction(Label label, Instruction instruction) {
        this.label = label;
        this.instruction = instruction;
    }
}

class InstructionList {
    SimpleLinkedList<LabeledInstruction> list = new SimpleLinkedList<LabeledInstruction>();

    public InstructionList() {
    }

    public void add(Instruction ins) {
        list.add(new LabeledInstruction(null, ins));
    }
}

