package intermediate;

public class Temp {
    private int id;
    public Temp ref;

    private Temp(int id, Temp ref) {
        this.id = id;
        this.ref = ref;
    }

    private static int count = 0;
    
    public static Temp newTemp(Temp ref) {
        return new Temp(count++, ref);
    }

    public static Temp newTemp() {
        return newTemp(null);
    }

    public boolean isRef() {
        return ref != null;
    }

    public String toString() {
        return "t" + new Integer(id).toString();
    }
}

