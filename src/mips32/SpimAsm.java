package mips32;

import intermediate.*;
import regalloc.*;
import java.io.*;
import java.util.Map;

class SpimAsm {
    InstructionList list;
    IR ir;
    Map<Temp, Register> map;

    public SpimAsm(InstructionList list, Map<Temp, Register> regMap, IR ir) {
        this.list = list;
        this.ir = ir;
        this.map = regMap;
    }

    public void output(BufferedWriter writer) throws IOException {
        writeTextSeg(writer);
        writer.newLine();
        writer.newLine();
        writeDataSeg(writer);
    }

    private void writeTextSeg(BufferedWriter writer) throws IOException {
        writer.write("\t.text");
        writer.newLine();
        writer.write("\t.align 2");
        writer.newLine();
        writer.write("\t.globl main");
        writer.newLine();
        writer.write("main:");
        writer.newLine();

        for (LabeledInstruction li: list) {
            if (li.label != null) {
                writer.write(li.label.toString() + ":");
                writer.newLine();
            }
            if (li.instruction != null) {
                writer.write("\t" + li.instruction.toString(map));
                writer.newLine();
            }
        }
    }

    private void writeDataSeg(BufferedWriter writer) throws IOException {
        writer.write("\t.data 0x10000000");
        writer.newLine();
        writer.write("\t.align 2");
        writer.newLine();
        for (int i = 0; i < ir.globalFrame.getSpilledLocalCount(); ++i) {
            writer.write("\t.word 0");
            writer.newLine();
        }
        for (arch.StringTable.StringPair pair: ir.stringTable) {
            writer.write(pair.name + ":");
            writer.newLine();
            writer.write("\t.asciiz \"" + escape(pair.value) + "\"");
            writer.newLine();
        }
    }

    public String escape(String s) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\t')
                ret.append("\\t");
            else if (c == '\n')
                ret.append("\\n");
            else if (c == '\\')
                ret.append("\\\\");
            else
                ret.append(c);
        }
        return ret.toString();
    }
}

