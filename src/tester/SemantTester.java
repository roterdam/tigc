package tester;

import absyn.*;
import parser.*;
import scanner.*;
import notifier.Notifier;
import semant.Semant;
import java.io.*;

public class SemantTester {
    private static boolean isTigerFile(String filePath) {
        return filePath.substring(filePath.lastIndexOf('.') + 1).equals("tig");
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing filepath");
            return;
        }

        File[] files = null;
        if (isTigerFile(args[0]))
            files = new File[]{new File(args[0])};
        else {
            files = new File(args[0]).listFiles();
            if (files == null) {
                System.out.println("No file to comiple");
                System.exit(1);
            }
        }

        for (File f: files) {
            if (!f.isFile())
                continue;

            String source = f.getAbsolutePath();
            if (!isTigerFile(source))
                continue;
            
            Notifier notifier = new Notifier(System.err);
            System.out.print("Testing " + source + ": ");

            FileReader reader = null;
            try {
                reader = new FileReader(source);
            } catch (FileNotFoundException e) {
                notifier.error(e.getMessage());
                continue;
            }

/*            FileWriter writer = null;
            try {
                writer = new FileWriter(source.substring(0, source.lastIndexOf('.')) + ".abs");
            } catch (IOException e) {
                notifier.error(e.getMessage());
                continue;
            }*/

//            Printer printer = new Printer();
            Parser parser = new Parser(new Scanner(reader), notifier);
            try {
                Absyn absyn = (Absyn)parser.parse().value;
                if (!notifier.hasError()) {
//                    printer.print((Expr)absyn, writer);
//                    writer.close();
                    Semant semant = new Semant(notifier);
                    semant.translate((Expr) absyn);
                    if (!notifier.hasError())
                        System.out.println("OK");
                }
            }
            catch (Exception e) {
                notifier.error(e.getMessage());
                e.printStackTrace();
                continue;
            }
        }
    }
}

