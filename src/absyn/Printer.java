package absyn;

import java.io.Writer;
import java.io.IOException;

public class Printer {
    public void print(Expr e, Writer writer) throws IOException {
        printExpr(e, 0, writer);
    }

    private void beginner(int level, Writer writer) throws IOException {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < level * 2; ++i)
            s.append(" ");
        writer.write(s.toString());
    }

    private void ender(int level, Writer writer) throws IOException {
    }

    private void printExpr(Expr e, int level, Writer writer) throws IOException {
        if (e instanceof ArrayExpr)
            printArrayExpr((ArrayExpr) e, level, writer);
        else if (e instanceof AssignmentExpr)
            printAssignmentExpr((AssignmentExpr) e, level, writer);
        else if (e instanceof BreakExpr)
            printBreakExpr((BreakExpr) e, level, writer);
        else if (e instanceof CallExpr)
            printCallExpr((CallExpr) e, level, writer);
        else if (e instanceof ForExpr)
            printForExpr((ForExpr) e, level, writer);
        else if (e instanceof IfExpr)
            printIfExpr((IfExpr) e, level, writer);
        else if (e instanceof IntExpr)
            printIntExpr((IntExpr) e, level, writer);
        else if (e instanceof LetExpr)
            printLetExpr((LetExpr) e, level, writer);
        else if (e instanceof LValueExpr)
            printLValueExpr((LValueExpr) e, level, writer);
        else if (e instanceof NegationExpr)
            printNegationExpr((NegationExpr) e, level, writer);
        else if (e instanceof NilExpr)
            printNilExpr((NilExpr) e, level, writer);
        else if (e instanceof OpExpr)
            printOpExpr((OpExpr) e, level, writer);
        else if (e instanceof RecordExpr)
            printRecordExpr((RecordExpr) e, level, writer);
        else if (e instanceof SeqExpr)
            printSeqExpr((SeqExpr) e, level, writer);
        else if (e instanceof StringExpr)
            printStringExpr((StringExpr) e, level, writer);
        else if (e instanceof WhileExpr)
            printWhileExpr((WhileExpr) e, level, writer);
    }

    private void printArrayExpr(ArrayExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("ArrayExpr " + e.type.toString() + "\n");
        printExpr(e.size, level + 1, writer);
        printExpr(e.init, level + 1, writer);
        ender(level, writer);
    }

    private void printAssignmentExpr(AssignmentExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("AssignmentExpr\n");
        printLValue(e.lvalue, level + 1, writer);
        printExpr(e.e, level + 1, writer);
        ender(level, writer);
    }

    private void printBreakExpr(BreakExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("BreakExpr\n");
        ender(level, writer);
    }

    private void printCallExpr(CallExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("CallExpr " + e.func.toString() + "\n");
        printExprList(e.args, level + 1, writer);
        ender(level, writer);
    }

    private void printForExpr(ForExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("ForExpr " + e.var.toString() + "\n");
        printExpr(e.begin, level + 1, writer);
        printExpr(e.end, level + 1, writer);
        printExpr(e.body, level + 1, writer);
        ender(level, writer);
    }

    private void printIfExpr(IfExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("IfExpr\n");
        printExpr(e.condition, level + 1, writer);
        printExpr(e.thenClause, level + 1, writer);
        if (e.elseClause != null)
            printExpr(e.elseClause, level + 1, writer);
        ender(level, writer);
    }

    private void printIntExpr(IntExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("IntExpr " + e.value.toString() + "\n");
        ender(level, writer);
    }

    private void printLetExpr(LetExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("LetExpr\n");
        printDeclList(e.decls, level + 1, writer);
        printExprList(e.exprs, level + 1, writer);
        ender(level, writer);
    }

    private void printLValueExpr(LValueExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("LValueExpr\n");
        printLValue(e.lvalue, level + 1, writer);
        ender(level, writer);
    }

    private void printNegationExpr(NegationExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("NegationExpr\n");
        printExpr(e.value, level + 1, writer);
        ender(level, writer);
    }

    private void printNilExpr(NilExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("NilExpr\n");
        ender(level, writer);
    }

    private void printOpExpr(OpExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("OpExpr " + e.op.toString() + "\n");
        printExpr(e.left, level + 1, writer);
        printExpr(e.right, level + 1, writer);
        ender(level, writer);
    }

    private void printRecordExpr(RecordExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("RecordExpr " + e.type.toString() + "\n");
        printFieldList(e.fields, level + 1, writer);
        ender(level, writer);
    }

    private void printSeqExpr(SeqExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("SeqExpr\n");
        printExprList(e.exprList, level + 1, writer);
        ender(level, writer);
    }

    private void printStringExpr(StringExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("StringExpr " + e.value.toString() + "\n");
        ender(level, writer);
    }

    private void printWhileExpr(WhileExpr e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("WhileExpr\n");
        printExpr(e.condition, level + 1, writer);
        printExpr(e.body, level + 1, writer);
        ender(level, writer);
    }

    private void printExprList(ExprList e, int level, Writer writer) throws IOException {
        if (e == null)
            return;

        beginner(level, writer);
        writer.write("ExprList\n");
        printExpr(e.expr, level + 1, writer);
        printExprList(e.next, level + 1, writer);
        ender(level, writer);
    }

    private void printLValue(LValue e, int level, Writer writer) throws IOException {
        if (e instanceof VarLValue)
            printVarLValue((VarLValue) e, level, writer);
        else if (e instanceof FieldLValue)
            printFieldLValue((FieldLValue) e, level, writer);
        else if (e instanceof SubscriptLValue)
            printSubscriptLValue((SubscriptLValue) e, level, writer);
    }

    private void printVarLValue(VarLValue e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("VarLValue " + e.name.toString() + "\n");
        ender(level, writer);
    }

    private void printFieldLValue(FieldLValue e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("FieldLValue " + e.id.toString() + "\n");
        printLValue(e.lvalue, level + 1, writer);
        ender(level, writer);
    }

    private void printSubscriptLValue(SubscriptLValue e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("SubscriptLValue\n");
        printLValue(e.lvalue, level + 1, writer);
        printExpr(e.expr, level + 1, writer);
        ender(level, writer);
    }

    private void printFieldList(FieldList e, int level, Writer writer) throws IOException {
        if (e == null)
            return;

        beginner(level, writer);
        writer.write("FieldList " + e.name.toString() + "\n");
        printExpr(e.value, level + 1, writer);
        printFieldList(e.next, level + 1, writer);
        ender(level, writer);
    }

    private void printDeclList(DeclList e, int level, Writer writer) throws IOException {
        if (e == null)
            return;

        beginner(level, writer);
        writer.write("DeclList\n");
        printDecl(e.decl, level + 1, writer);
        printDeclList(e.next, level + 1, writer);
        ender(level, writer);
    }

    private void printDecl(Decl e, int level, Writer writer) throws IOException {
        if (e instanceof TypeDecl)
            printTypeDecl((TypeDecl) e, level, writer);
        else if (e instanceof VarDecl)
            printVarDecl((VarDecl) e, level, writer);
        else if (e instanceof FuncDecl)
            printFuncDecl((FuncDecl) e, level, writer);
    }

    private void printTypeDecl(TypeDecl e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("TypeDecl " + e.name.toString() + "\n");
        printType(e.type, level + 1, writer);
        ender(level, writer);
    }

    private void printVarDecl(VarDecl e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("VarDecl " + e.id.toString());
        if (e.type != null)
            writer.write(", " + e.type.toString());
        writer.write("\n");
        printExpr(e.value, level + 1, writer);
        ender(level, writer);
    }

    private void printFuncDecl(FuncDecl e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("FuncDecl " + e.name.toString());
        if (e.type != null)
            writer.write(", " + e.type.toString());
        writer.write("\n");
        printTypeFields(e.params, level + 1, writer);
        printExpr(e.body, level + 1, writer);
        ender(level, writer);
    }

    private void printType(Type e, int level, Writer writer) throws IOException {
        if (e instanceof NameType)
            printNameType((NameType) e, level, writer);
        else if (e instanceof ArrayType)
            printArrayType((ArrayType) e, level, writer);
        else if (e instanceof RecordType)
            printRecordType((RecordType) e, level, writer);
    }

    private void printNameType(NameType e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("NameType " + e.name.toString() + "\n");
        ender(level, writer);
    }

    private void printArrayType(ArrayType e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("ArrayType " + e.base.toString() + "\n");
        ender(level, writer);
    }

    private void printRecordType(RecordType e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("RecordType\n");
        printTypeFields(e.fields, level + 1, writer);
        ender(level, writer);
    }

    private void printTypeFields(TypeFields e, int level, Writer writer) throws IOException {
        if (e == null)
            return;

        beginner(level, writer);
        writer.write("TypeFields\n");
        printTypeField(e.head, level + 1, writer);
        printTypeFields(e.next, level + 1, writer);
        ender(level, writer);
    }

    private void printTypeField(TypeField e, int level, Writer writer) throws IOException {
        beginner(level, writer);
        writer.write("TypeField " + e.name.toString() + ", " + e.type.toString() + "\n");
        ender(level, writer);
    }
}

