package frame;

import intermediate.*;
import java.util.*;
import arch.Const;

public class Frame {
    public Temp returnValue = null;
    public ArrayList<Temp> params = new ArrayList<Temp>();
    public ArrayList<Temp> locals = new ArrayList<Temp>();
    public ArrayList<Label> returns = new ArrayList<Label>();
    public Label place;
    public Temp display;
    public Const frameSize = new Const(0), minusFrameSize = new Const(0);

    public Frame(Label place, Temp display) {
        this.place = place;
        this.display = display;
    }

    public void updateFrameSize(int wordLength) {
        int l = params.size() + 4;
        frameSize.bind(l * wordLength);
        minusFrameSize.bind(-l * wordLength);
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

