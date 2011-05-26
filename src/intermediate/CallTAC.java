package intermediate;

import frame.Frame;

public class CallTAC extends ThreeAddressCode {
    public Label place;

    public CallTAC(Frame frame, Label place) {
        super(frame);
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
        this.place = place;
    }

    public String toString() {
        return "call " + place.toString();
    }
}

