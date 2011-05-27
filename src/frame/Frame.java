package frame;

import intermediate.*;
import java.util.*;

public class Frame {
    public Temp returnValue = null;
    public ArrayList<Temp> params = new ArrayList<Temp>();
    public ArrayList<Temp> locals = new ArrayList<Temp>();
    public ArrayList<Label> returns = new ArrayList<Label>();
    public Label place;

    public Frame(Label place) {
        this.place = place;
    }

    public Temp addLocal() {
        Temp t = Temp.newTemp(this);
        locals.add(t);
        return t;
    }

    public Temp addParam() {
        Temp t = Temp.newTemp(this);
        params.add(t);
        return t;
    }

    public Temp addReturnValue() {
        if (returnValue != null)
            return returnValue;
        else {
            Temp t = Temp.newTemp(this);
            returnValue = t;
            return t;
        }
    }
}

