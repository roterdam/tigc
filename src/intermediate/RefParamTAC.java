package intermediate;

public class RefParamTAC extends ThreeAddressCode {
    public RefParamTAC(AssignableAccess actual, TempAccess formal) {
        this.op1 = (Access) actual;
        this.op2 = null;
        this.dst = formal;
    }

    public String toString() {
        return "ref param " + op1.toString() + " => " + dst.toString();
    }
}

