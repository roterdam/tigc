package absyn;

public class LetExpr extends Expr {
    public DeclList decls;
    public ExprList exprs;

    public LetExpr(int pos, DeclList decls, ExprList exprs) {
        super(pos);
        this.decls = decls;
        this.exprs = exprs;
    }
}

