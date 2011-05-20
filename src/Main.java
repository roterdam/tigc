import scanner.Scanner;
import parser.Parser;
import error.ErrorMsg;
import absyn.*;
import symbol.*;
import semant.Semant;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        ErrorMsg error = new ErrorMsg(System.out);
        if (args.length == 0) {
            error.report("Missing filepath");
            return;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(args[0]);
        } catch (FileNotFoundException e) {
            error.report(e.getMessage());
            return;
        }

        Parser parser = new Parser(new Scanner(reader), error);
        try {
            Object absyn = parser.parse().value;
            if (!error.hasError()) {
                Semant semant = new Semant(error);
                semant.translate((Expr) absyn);
            }
        }
        catch (Exception e) {
            error.report(e.getMessage());
            e.printStackTrace();
        }
    }
}

