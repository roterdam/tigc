package intermediate;

public class ConstAccess implements SimpleAccess {
    public int value;
    
    public ConstAccess(int value) {
        this.value = value;
    }

    public String toString() {
        return "#" + new Integer(value).toString();
    }
}

