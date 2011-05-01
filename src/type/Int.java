package type;

public class Int extends Type {
    public Int() {
    }

    public boolean fits(Type type) {
        return type.actual() instanceof Int;
    }

    public java.lang.String toString() {
        return "INT";
    }
}

