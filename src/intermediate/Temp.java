package intermediate;

import frame.*;

public class Temp implements AssignableAccess, SimpleAccess {
    private int id;
    public Frame frame;

    private Temp(int id, Frame frame) {
        this.id = id;
        this.frame = frame;
    }

    private static int count = 0;
    
    public static Temp newTemp(Frame frame) {
        return new Temp(count++, frame);
    }

    public String toString() {
        return "t" + new Integer(id).toString();
    }

    public boolean equals(Object o) {
        if (o instanceof Temp)
            return ((Temp) o).id == this.id;
        else
            return false;
    }
}

