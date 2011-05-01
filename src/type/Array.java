package type;

public class Array extends Type {
    public Type base;

    public Array(Type base) {
        this.base = base;
    }

    public boolean fits(Type type) {
        return this == type.actual();
    }

    public java.lang.String toString() {
        return "ARRAY";
    }
}

