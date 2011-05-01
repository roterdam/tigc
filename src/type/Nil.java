package type;

public class Nil extends Type {
    public Nil() {
    }

    public boolean fits(Type type) {
        Type a = type.actual();
        return (a instanceof Record) || (a instanceof Nil);
    }

    public java.lang.String toString() {
        return "NIL";
    }
}

