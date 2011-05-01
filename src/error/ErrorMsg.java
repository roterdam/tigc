package error;

public class ErrorMsg {
    private java.io.PrintStream stream;
    private int errorCount = 0;

    public ErrorMsg(java.io.PrintStream s) {
        this.stream = s;
    }

    public void report(String message) {
        ++errorCount;
        stream.println(message);
    }

    public void report(String message, int line) {
        report(new Integer(line + 1).toString() + ": " + message);
    }

    public boolean hasError() {
        return errorCount > 0;
    }
}
