package intermediate;

public class MoveTAC extends ThreeAddressCode {
    public MoveTAC(Access src, AssignableAccess dst) {
        this.op1 = src;
        this.op2 = null;
        this.dst = (Access)dst;
    }

    public String toString() {
        return dst.toString() + " := " + op1.toString();
    }
}
 
