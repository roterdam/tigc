package intermediate;

import frame.Frame;

public class ReturnTAC extends ThreeAddressCode {
    public ReturnTAC(Frame frame) {
        super(frame);
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
    }

    public String toString() {
        return "return";
    }
}

