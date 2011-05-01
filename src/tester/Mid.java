package tester;

import absyn.*;
import parser.*;
import scanner.*;
import error.*;
import semant.Semant;
import java.io.*;

public class Mid {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing filepath");
            System.exit(1);
        }

        String source = args[0];

        ErrorMsg error = new ErrorMsg(System.out);

        FileReader reader = null;
        try {
            reader = new FileReader(source);
        } catch (FileNotFoundException e) {
            error.report(e.getMessage());
            System.exit(1);
        }

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(source.substring(0, source.lastIndexOf('.')) + ".abs"));
        } catch (IOException e) {
            error.report(e.getMessage());
            System.exit(1);
        }

        Printer printer = new Printer();
        Parser parser = new Parser(new Scanner(reader), error);
        try {
            Object absyn = parser.parse().value;
            if (!error.hasError()) {
                printer.print((Expr) absyn, writer);
                writer.close();
                Semant semant = new Semant(error);
                semant.translate((Expr) absyn);
            }
        }
        catch (Exception e) {
            error.report(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        if (!error.hasError())
            System.exit(0);
        else
            System.exit(1);
    }
}

