package intermediate;

public class GotoTAC extends ThreeAddressCode {
    public Label place;

    public GotoTAC(Label place) {
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
        this.place = place;
    }

    public String toString() {
        return "goto " + place.toString();
    }
}

