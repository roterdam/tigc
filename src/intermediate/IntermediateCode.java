package intermediate;

public class IntermediateCode {
    public Label label;
    public ThreeAddressCode tac;
    public String comment;

    public IntermediateCode(Label label, ThreeAddressCode tac, String comment) {
        this.label = label;
        this.tac = tac;
        this.comment = comment;
    }

    public IntermediateCode(Label label, ThreeAddressCode tac) {
        this(label, tac, "");
    }

    public IntermediateCode(Label label) {
        this(label, null);
    }

    public String toString() {
        String ret = "";
        if (label != null)
            ret += label.toString() + ":";
        ret += "\t";
        if (tac != null)
            ret += tac.toString();
        if (comment != null)
            ret += " //" + comment;
        return ret;
    }
}


