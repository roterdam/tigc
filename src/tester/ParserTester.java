package tester;

import absyn.*;
import parser.*;
import scanner.*;
import notifier.*;
import java.io.*;

public class ParserTester {
    public static void main(String[] args) {
        for (int i = 1; i <= 49; ++i) {
            Notifier notifier = new Notifier(System.err);

            String source = "progs/test" + new Integer(i).toString() + ".tig";
            System.out.print("Testing " + source + ": ");

            FileReader reader = null;
            try {
                reader = new FileReader(source);
            } catch (FileNotFoundException e) {
                notifier.reportError(e.getMessage());
                continue;
            }

            FileWriter writer = null;
            try {
                writer = new FileWriter(source.substring(0, source.lastIndexOf('.')) + ".abs");
            } catch (IOException e) {
                notifier.reportError(e.getMessage());
                continue;
            }

            Printer printer = new Printer();
            Parser parser = new Parser(new Scanner(reader), notifier);
            try {
                Absyn absyn = (Absyn)parser.parse().value;
                if (!notifier.hasError()) {
                    printer.print((Expr)absyn, writer);
                    writer.close();
                    System.out.println("OK");
                }
            }
            catch (Exception e) {
                notifier.reportError(e.getMessage());
                continue;
            }
        }
    }
}

