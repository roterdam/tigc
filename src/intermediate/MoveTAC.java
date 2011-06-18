package intermediate;

import frame.Frame;

public class MoveTAC extends ThreeAddressCode {
    public MoveTAC(Frame frame, Access src, AssignableAccess dst) {
        super(frame);
        this.op1 = src;
        this.op2 = null;
        this.dst = (Access)dst;
    }

    public String toString() {
        return dst.toString() + " := " + op1.toString();
    }

    public MoveTAC clone() {
        return new MoveTAC(frame, op1, (AssignableAccess) dst);
    }
}
 
