package intermediate;

public class MoveTAC extends ThreeAddressCode {
    public MoveTAC(Access src, TempAccess dst) {
        this.op1 = src;
        this.op2 = null;
        this.dst = dst;
    }

    public MoveTAC(Access src, MemAccess dst) {
        this.op1 = src;
        this.op2 = null;
        this.dst = dst;
    }

    public String toString() {
        return dst.toString() + " := " + op1.toString();
    }
}
 
