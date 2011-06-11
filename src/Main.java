import scanner.Scanner;
import parser.Parser;
import notifier.Notifier;
import absyn.*;
import symbol.*;
import semant.Semant;
import java.io.*;
import intermediate.*;
import mips32.CodeGen;
import mips32.Optimizer;

public class Main {
    public static String removeExtensionName(String filename) {
        int i = filename.lastIndexOf('.');
        if (i == -1)
            return filename;
        else
            return filename.substring(0, i);
    }

    public static void main(String[] args) {
        Notifier notifier = new Notifier(System.out);
        if (args.length == 0) {
            notifier.error("Missing filepath");
            return;
        }

        String srcFile = args[0];
        FileReader reader = null;
        try {
            reader = new FileReader(srcFile);
        } catch (FileNotFoundException e) {
            notifier.error(e.getMessage());
            return;
        }

        Parser parser = new Parser(new Scanner(reader), notifier);
        try {
            java_cup.runtime.Symbol absyn = parser.parse();
            if (!notifier.hasError()) {
                Semant semant = new Semant(notifier);
                IR ir = semant.translate((Expr) absyn.value);

                if (!notifier.hasError()) {
                    /*for (IntermediateCode c: ir.codes) {
                        notifier.message(c.toString());
                    }
                    notifier.message("");
                    notifier.message("");*/

                    Optimizer opt = new Optimizer();
                    CodeGen cg = new CodeGen(notifier, ir, opt);

                    if (!notifier.hasError()) {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(removeExtensionName(srcFile) + ".s"));
                        cg.generate(writer);
                        writer.close();
                    }

                }
            }
            
            if (notifier.hasError()) {
                notifier.printSummary();
            }
        }
        catch (Exception e) {
            notifier.error(e.getMessage());
            e.printStackTrace();
        }
    }
}

