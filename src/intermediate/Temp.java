package intermediate;

public class Temp {
    private int id;
    
    private Temp(int id) {
        this.id = id;
    }

    private static int count = 0;
    
    public static Temp newTemp() {
        return new Temp(count++);
    }

    public String toString() {
        return "t" + new Integer(id).toString();
    }
}

