package intermediate;

import frame.Frame;
import java.util.ArrayList;

public class CallTAC extends ThreeAddressCode {
    public Label place;
    public ArrayList<Param> params = new ArrayList<Param>();

    public static class Param {
        public Access actual;
        public Temp formal;

        public Param(Access actual, Temp formal) {
            this.actual = actual;
            this.formal = formal;
        }
    }

    public CallTAC(Frame frame, Label place) {
        super(frame);
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
        this.place = place;
    }

    public void addParam(Access actual, Temp formal) {
        params.add(new Param(actual, formal));
    }

    public String toString() {
        String s = "call " + place.toString() + "(";
        String ss = "";
        for (Param p: params) {
            if (ss.length() > 0)
                ss += ", ";
            ss += p.formal.toString() + " <= " + p.actual.toString();
        }
        s += ss + ")";
        return s;
    }
}

