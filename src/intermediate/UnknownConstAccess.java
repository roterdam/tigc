package intermediate;

public class UnknownConstAccess extends ConstAccess implements SimpleAccess {
    public String name;
    private boolean binded = false;

    public UnknownConstAccess(String name) {
        super(0);
        this.name = name;
    }

    public void bind(int value) {
        this.value = value;
        binded = true;
    }

    public String toString() {
        if (binded)
            return super.toString();
        else
            return name;
    }
}

