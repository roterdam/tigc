package intermediate;

import frame.Frame;

public class UniOpTAC extends OpTAC {
    public enum UniOp {
        NOT, NEG
    }

    public UniOp op;
    
    public UniOpTAC(Frame frame, UniOp op, Access op1, AssignableAccess dst) {
        super(frame);
        this.op = op;
        this.op1 = op1;
        this.op2 = null;
        this.dst = (Access) dst;
    }

    public String toString() {
        String s = "";
        switch (op) {
            case NOT:
                s = "~";
                break;

            case NEG:
                s = "-";
                break;
        }
        return dst.toString() + " := " + s + op1.toString();
    }
}

