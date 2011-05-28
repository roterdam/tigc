import scanner.Scanner;
import parser.Parser;
import notifier.Notifier;
import absyn.*;
import symbol.*;
import semant.Semant;
import java.io.*;
import intermediate.*;
import mips32.CodeGen;

public class Main {
    public static void main(String[] args) {
        Notifier notifier = new Notifier(System.out);
        if (args.length == 0) {
            notifier.error("Missing filepath");
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(args[0]);
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
                    for (IntermediateCode c: ir.codes) {
                        notifier.message(c.toString());
                    }
                    notifier.message("");
                    notifier.message("");

                    CodeGen cg = new CodeGen(notifier, ir);
                    cg.generate();

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

