package intermediate;

import frame.Frame;

public class GotoTAC extends ThreeAddressCode {
    public Label place;

    public GotoTAC(Frame frame, Label place) {
        super(frame);
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
        this.place = place;
    }

    public String toString() {
        return "goto " + place.toString();
    }
}

