package arch;

import intermediate.Label;
import java.util.*;

public abstract class InstructionList {
    public abstract void addAllBefore(List<Label> frontLabels, List<Instruction> list, Label beforeLabel);
    public abstract void replaceLabel(List<Label> oldLabels, Label newLabel);
    public abstract String toString();
    public abstract void redirect(Instruction ins, List<Label> oldPlace, Label newPlace);
}

