import scanner.Scanner;
import parser.Parser;
import notifier.Notifier;
import absyn.*;
import symbol.*;
import semant.Semant;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        Notifier notifier = new Notifier(System.out);
        if (args.length == 0) {
            notifier.reportError("Missing filepath");
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(args[0]);
        } catch (FileNotFoundException e) {
            notifier.reportError(e.getMessage());
            return;
        }

        Parser parser = new Parser(new Scanner(reader), notifier);
        try {
            Object absyn = parser.parse().value;
            if (!notifier.hasError()) {
                Semant semant = new Semant(notifier);
                semant.translate((Expr) absyn);
            } else {
                notifier.reportSummary();
            }
        }
        catch (Exception e) {
            notifier.reportError(e.getMessage());
            e.printStackTrace();
        }
    }
}

