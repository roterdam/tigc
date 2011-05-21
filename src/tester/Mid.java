package tester;

import absyn.*;
import parser.*;
import scanner.*;
import notifier.*;
import semant.Semant;
import java.io.*;

public class Mid {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing filepath");
            System.exit(1);
        }

        String source = args[0];

        Notifier notifier = new Notifier(System.out);

        FileReader reader = null;
        try {
            reader = new FileReader(source);
        } catch (FileNotFoundException e) {
            notifier.error(e.getMessage());
            System.exit(1);
        }

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(source.substring(0, source.lastIndexOf('.')) + ".abs"));
        } catch (IOException e) {
            notifier.error(e.getMessage());
            System.exit(1);
        }

        Printer printer = new Printer();
        Parser parser = new Parser(new Scanner(reader), notifier);
        try {
            Object absyn = parser.parse().value;
            if (!notifier.hasError()) {
                printer.print((Expr) absyn, writer);
                writer.close();
                Semant semant = new Semant(notifier);
                semant.translate((Expr) absyn);
            }
        }
        catch (Exception e) {
            notifier.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        if (!notifier.hasError())
            System.exit(0);
        else
            System.exit(1);
    }
}

