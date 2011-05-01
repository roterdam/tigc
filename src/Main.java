import scanner.Scanner;
import parser.Parser;
import error.ErrorMsg;
import absyn.*;
import symbol.*;
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
            Object sym = parser.parse();
        }
        catch (Exception e) {
            error.report(e.getMessage());
        }
    }
}

