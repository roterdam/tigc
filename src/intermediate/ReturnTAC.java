package intermediate;

public class ReturnTAC extends ThreeAddressCode {
    public ReturnTAC() {
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
    }

    public String toString() {
        return "return";
    }
}

