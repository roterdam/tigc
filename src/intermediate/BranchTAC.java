package intermediate;

public class BranchTAC extends ThreeAddressCode {
    public enum BranchType {
        EQ, NEQ, LT, LEQ, GT, GEQ
    }

    public BranchType type;
    public Label place;

    public BranchTAC(BranchType type, Access op1, Access op2, Label place) {
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
        this.dst = null;
        this.place = place;
    }

    public String toString() {
        String s = "";
        switch (type) {
            case EQ:
                s = "==";
                break;

            case NEQ:
                s = "!=";
                break;

            case LT:
                s = "<";
                break;

            case LEQ:
                s = "<=";
                break;

            case GT:
                s = ">";
                break;

            case GEQ:
                s = ">=";
                break;
        }
        return "if " + op1.toString() + s + op2.toString() + " then goto " + place.toString();
    }
}

