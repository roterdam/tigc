package type;

public abstract class Type {
    public Type actual() {
        return this;
    }

    public boolean fits(Type type) {
        return false;
    }

    public java.lang.String toString() {
        return "UNKNOWN_TYPE";
    }
}

