package notifier;

public class Notifier {
    private java.io.PrintStream stream;
    private int errorCount = 0;
    private int warningCount = 0;

    public Notifier(java.io.PrintStream s) {
        this.stream = s;
    }

    public void reportError(String message) {
        ++errorCount;
        stream.println("ERROR " + message);
    }

    public void reportError(String message, int line) {
        reportError(new Integer(line + 1).toString() + ": " + message);
    }

    public void reportWarning(String message) {
        ++warningCount;
        stream.println("WARNING " + message);
    }

    public void reportWarning(String message, int line) {
        reportWarning(new Integer(line + 1).toString() + ": " + message);
    }

    public void reportInfo(String info) {
        stream.println(info);
    }

    public void reportSummary() {
        String s = "";
        if (errorCount > 0)
            s += new Integer(errorCount).toString() + " error(s)";
        if (warningCount > 0) {
            if (s.length() > 0)
                s += ", ";
            s += new Integer(warningCount).toString() + " warning(s)";
        }
        if (s.length() > 0)
            stream.println(s);
    }

    public boolean hasError() {
        return errorCount > 0;
    }
}
