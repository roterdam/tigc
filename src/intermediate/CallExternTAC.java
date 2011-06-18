package intermediate;

import frame.Frame;

public class CallExternTAC extends ThreeAddressCode {
    public symbol.Symbol place;
    public Access param1, param2, param3;

    public CallExternTAC(Frame frame, symbol.Symbol place, Access param1, Access param2, Access param3, AssignableAccess dst) {
        super(frame);
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.op1 = null;
        this.op2 = null;
        this.dst = (Access)dst;
        this.place = place;
    }

    public CallExternTAC clone() {
        return new CallExternTAC(frame, place, param1, param2, param3, (AssignableAccess) dst);
    }

    public String toString() {
        String s = "call extern " + place.toString() + "(";
        if (param1 != null)
            s += param1.toString();
        if (param2 != null)
            s += ", " + param2.toString();
        s += ")";
        if (dst == null)
            return s;
        else
            return dst.toString() + " := " + s;
    }
}

