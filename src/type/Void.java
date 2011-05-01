package type;

public class Void extends Type {
    public Void() {
    }

    public boolean fits(Type type) {
        return type.actual() instanceof Void;
    }

    public java.lang.String toString() {
        return "VOID";
    }
}

