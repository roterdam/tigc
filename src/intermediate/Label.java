package intermediate;

public class Label {
    private int id;

    private Label(int id) {
        this.id = id;
    }

    private static int count = 0;

    public static Label newLabel() {
        return new Label(count++);
    }

    public String toString() {
        return "L" + new Integer(id).toString();
    }

    public boolean equals(Object x) {
        if (!(x instanceof Label))
            return false;
        return this.id == ((Label) x).id;
    }
}

