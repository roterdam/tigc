package tester;

import absyn.*;
import parser.*;
import scanner.*;
import error.*;
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
            
            ErrorMsg error = new ErrorMsg(System.err);
            System.out.print("Testing " + source + ": ");

            FileReader reader = null;
            try {
                reader = new FileReader(source);
            } catch (FileNotFoundException e) {
                error.report(e.getMessage());
                continue;
            }

/*            FileWriter writer = null;
            try {
                writer = new FileWriter(source.substring(0, source.lastIndexOf('.')) + ".abs");
            } catch (IOException e) {
                error.report(e.getMessage());
                continue;
            }*/

//            Printer printer = new Printer();
            Parser parser = new Parser(new Scanner(reader), error);
            try {
                Absyn absyn = (Absyn)parser.parse().value;
                if (!error.hasError()) {
//                    printer.print((Expr)absyn, writer);
//                    writer.close();
                    Semant semant = new Semant(error);
                    semant.translate((Expr) absyn);
                    if (!error.hasError())
                        System.out.println("OK");
                }
            }
            catch (Exception e) {
                error.report(e.getMessage());
                e.printStackTrace();
                continue;
            }
        }
    }
}

