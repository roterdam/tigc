package mips32;

import intermediate.*;
import util.*;
import java.util.*;

public class CodeGen {
    HashMap<Label, ThreeAddressCode> labelMap;
    IR ir;

    public CodeGen(IR ir) {
        this.ir = ir;
        labelMap = new HashMap<Label, ThreeAddressCode>();
        
        HashSet<Label> labels = new HashSet<Label>();
        for (IntermediateCode ic: ir.codes) {
            if (ic.label != null)
                labels.add(ic.label);
            if (ic.tac != null) {
                for (Label l: labels)
                    labelMap.put(l, ic.tac);
                labels.clear();
            }
        }
    }
}

