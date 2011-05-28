package notifier;

public class Notifier {
    private java.io.PrintStream stream;
    private int errorCount = 0;
    private int warningCount = 0;

    public Notifier(java.io.PrintStream s) {
        this.stream = s;
    }

    private void println(String message) {
        stream.println(message);
    }

    public void error(String message) {
        ++errorCount;
        println("ERROR: " + message);
    }

    public void error(String message, int line) {
        ++errorCount;
        println(new Integer(line + 1).toString() + ": ERROR: " + message);
    }

    public void warning(String message) {
        ++warningCount;
        println("WARNING: " + message);
    }

    public void warning(String message, int line) {
        ++warningCount;
        println(new Integer(line + 1).toString() + ": WARNING: " + message);
    }

    public void message(String info) {
        println(info);
    }

    public void printSummary() {
        String s = "";
        if (errorCount > 0)
            s += new Integer(errorCount).toString() + " error(s)";
        if (warningCount > 0) {
            if (s.length() > 0)
                s += ", ";
            s += new Integer(warningCount).toString() + " warning(s)";
        }
        if (s.length() > 0)
            println("Compile failed: " + s);
    }

    public boolean hasError() {
        return errorCount > 0;
    }
}

