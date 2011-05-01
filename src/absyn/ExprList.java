package absyn;

public class ExprList extends Absyn {
    public Expr expr;
    public ExprList next;

    public ExprList(int pos, Expr expr, ExprList next) {
        super(pos);
        this.expr = expr;
        this.next = next;
    }
}

