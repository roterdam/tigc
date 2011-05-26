package frame;

import intermediate.*;
import java.util.*;

public class Frame {
    public Temp returnValue = null;
    public ArrayList<Temp> params = new ArrayList<Temp>();
    public ArrayList<Temp> locals = new ArrayList<Temp>();
    public ArrayList<Label> returns = new ArrayList<Label>();

    public Frame() {
    }
}

