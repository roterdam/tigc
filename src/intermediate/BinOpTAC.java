package intermediate;

public class BinOpTAC extends OpTAC {
    public enum BinOp {
        ADD, SUB, MUL, DIV, AND, OR,
            EQ, NEQ, GT, GEQ, LT, LEQ
    }

    public BinOp op;

    public BinOpTAC(BinOp op, Access op1, Access op2, AssignableAccess dst) {
        this.op = op;
        this.op1 = op1;
        this.op2 = op2;
        this.dst = (Access) dst;
    }

    public String toString() {
        String s = "";
        switch (op) {
            case ADD:
                s = "+";
                break;

            case SUB:
                s = "-";
                break;

            case MUL:
                s = "*";
                break;

            case DIV:
                s = "/";
                break;

            case AND:
                s = "&";
                break;

            case OR:
                s = "|";
                break;

            case EQ:
                s = "==";
                break;

            case NEQ:
                s = "!=";
                break;

            case GT:
                s = ">";
                break;

            case GEQ:
                s = ">=";
                break;

            case LT:
                s = "<";
                break;

            case LEQ:
                s = "<=";
                break;
        }
        return dst.toString() + " := " + op1.toString() + " " + s + " " + op2.toString();
    }
}

