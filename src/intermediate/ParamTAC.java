package intermediate;

public class ParamTAC extends ThreeAddressCode {
    public ParamTAC(Access actual, TempAccess formal) {
        this.op1 = actual;
        this.op2 = null;
        this.dst = formal;
    }

    public String toString() {
        return "param " + op1.toString() + " => " + dst.toString();
    }
}

