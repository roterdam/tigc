package intermediate;

public class Label {
    private int id;
    String tag;

    private Label(int id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    private static int count = 0;

    public static Label newLabel(String tag) {
        return new Label(count++, tag);
    }

    public static Label newLabel() {
        return newLabel("");
    }

    public String toString() {
        String s = "L" + new Integer(id).toString();
        if (tag.length() > 0)
            s += "_" + tag;
        return s;
    }

    public boolean equals(Object x) {
        if (!(x instanceof Label))
            return false;
        return this.id == ((Label) x).id;
    }
}

