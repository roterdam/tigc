package type;

public class String extends Type {
    public String() {
    }

    public boolean fits(Type type) {
        return type.actual() instanceof String;
    }

    public java.lang.String toString() {
        return "STRING";
    }
}

