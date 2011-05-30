package regalloc;

public class Register {
    private String name;

    public Register(String name) {
        this.name = name.intern();
    }

    public boolean equals(Object o) {
        return hashCode() == o.hashCode();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }
}

