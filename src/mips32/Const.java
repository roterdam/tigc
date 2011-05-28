package mips32;

class Const {
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
}


