package intermediate;

import frame.Frame;
import java.util.ArrayList;

public class CallTAC extends ThreeAddressCode {
    public Label place;
    public ArrayList<Access> params = new ArrayList<Access>();
    public Temp actualReturn;

    public CallTAC(Frame frame, Label place, Temp actualReturn) {
        super(frame);
        this.op1 = null;
        this.op2 = null;
        this.dst = null;
        this.place = place;
        this.actualReturn = actualReturn;
    }

    public void addParam(Access actual) {
        params.add(actual);
    }

    public String toString() {
        String s = "call " + place.toString() + "(";
        String ss = "";
        for (Access a: params) {
            if (ss.length() > 0)
                ss += ", ";
            ss += a.toString();
        }
        s += ss + ")";
        if (actualReturn != null)
            s = actualReturn.toString() + " := " + s;
        return s;
    }
}

