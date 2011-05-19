package intermediate;

public class TempAccess extends Access implements AssignableAccess, SimpleAccess {
    public Temp temp;

    public TempAccess(Temp temp) {
        this.temp = temp;
    }

    public String toString() {
        return temp.toString();
    }
}

