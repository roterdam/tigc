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
    private Map<Temp, Integer> spilledLocals = new HashMap<Temp, Integer>();
    public boolean global = false;

    public Frame(Label place, Temp display, boolean global) {
        this.place = place;
        this.display = display;
        this.global = global;
    }

    public Frame(Label place, Temp display) {
        this(place, display, false);
    }

    public int getSpilledLocalCount() {
        return spilledLocals.size();
    }

    public void updateFrameSize(int wordLength) {
        int l = params.size() + 4 + spilledLocals.size();
        frameSize.bind(l * wordLength);
        minusFrameSize.bind(-l * wordLength);
    }

    public int spill(Temp t, int wordLength) {
        if (t == returnValue)
            return -(3 + params.size()) * wordLength;

        int i = params.indexOf(t);
        if (i != -1)
            return -(3 + i) * wordLength;

        int index = spilledLocals.size();
        if (!spilledLocals.containsKey(t)) {
            if (!global)
                i = -(4 + params.size() + index);
            else
                i = index;
            spilledLocals.put(t, new Integer(i));
            return i * wordLength;
        } else
            return spilledLocals.get(t).intValue() * wordLength;
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

