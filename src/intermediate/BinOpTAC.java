package intermediate;

import frame.Frame;

public class BinOpTAC extends OpTAC {
    public enum BinOp {
        ADD, SUB, MUL, DIV,
            EQ, NEQ, GT, GEQ, LT, LEQ
    }

    public BinOp op;

    public BinOpTAC(Frame frame, BinOp op, Access op1, Access op2, AssignableAccess dst) {
        super(frame);
        this.op = op;
        this.op1 = op1;
        this.op2 = op2;
        this.dst = (Access) dst;
    }

    public BinOpTAC clone() {
        return new BinOpTAC(frame, op, op1, op2, (AssignableAccess) dst);
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

