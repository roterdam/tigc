package intermediate;

public class IntermediateCodeList {
    public Label label;
    public ThreeAddressCode tac;
    public String comment;
    public IntermediateCodeList next;

    public IntermediateCodeList(Label label, ThreeAddressCode tac, String comment, IntermediateCodeList next) {
        this.label = label;
        this.tac = tac;
        this.comment = comment;
        this.next = next;
    }

    public IntermediateCodeList(Label label, ThreeAddressCode tac, String comment) {
        this(label, tac, comment, null);
    }

    public IntermediateCodeList(Label label, ThreeAddressCode tac) {
        this(label, tac, "");
    }

    public IntermediateCodeList(Label label) {
        this(label, null);
    }
}

