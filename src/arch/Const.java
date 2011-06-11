package arch;

public class Const {
    int c = 0;
    String name = "";
    boolean binded = true;

    public Const(int c) {
        this.c = c;
        binded = true;
    }

    public Const(String name) {
        this.name = name;
        binded = false;
    }

    public void bind(int c) {
        this.c = c;
        binded = true;
    }

    public String toString() {
        if (binded)
            return new Integer(c).toString();
        else
            return name;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Const))
            return false;
        Const x = (Const) o;
        if (binded)
            return c == x.c;
        else
            return name.equals(x.name);
    }
}


