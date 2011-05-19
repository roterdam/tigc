package intermediate;

public class ConstAccess extends Access implements SimpleAccess {
    public int value;
    
    public ConstAccess(int value) {
        this.value = value;
    }

    public String toString() {
        return "#" + new Integer(value).toString();
    }
}

