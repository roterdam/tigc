package tester;

import absyn.*;
import parser.*;
import scanner.*;
import notifier.*;
import semant.Semant;
import java.io.*;
import intermediate.*;
import mips32.*;

public class Final {
    private static String removeExtensionName(String filename) {
        int i = filename.lastIndexOf('.');
        if (i == -1)
            return filename;
        else
            return filename.substring(0, i);
    }

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

        Parser parser = new Parser(new Scanner(reader), notifier);
        try {
            Object absyn = parser.parse().value;
            if (!notifier.hasError()) {
                Semant semant = new Semant(notifier);
                IR ir = semant.translate((Expr) absyn);
                if (!notifier.hasError()) {
                    Optimizer opt = new Optimizer();
                    CodeGen cg = new CodeGen(notifier, ir, opt);

                    if (!notifier.hasError()) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(removeExtensionName(source) + ".s"));
                        cg.generate(writer);
                        writer.close();
                    }
                }
            }
        } catch (Exception e) {
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

