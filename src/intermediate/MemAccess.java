package intermediate;

public class MemAccess extends Access implements AssignableAccess {
    public SimpleAccess base, offset;

    public MemAccess(SimpleAccess base, SimpleAccess offset) {
        this.base = base;
        this.offset = offset;
    }

    public String toString() {
        return base.toString() + "(" + offset.toString() + ")";
    }
}

