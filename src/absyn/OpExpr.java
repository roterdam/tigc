package absyn;

public class OpExpr extends Expr {
    public static enum Op {
        ADD, SUB, MUL, DIV,
            EQ, NEQ, LT, LEQ,
            GT, GEQ, AND, OR
    }

    public Expr left, right;
    public Op op;

    public OpExpr(int pos, Op op, Expr left, Expr right) {
        super(pos);
        this.op = op;
        this.left = left;
        this.right = right;
    }
}
