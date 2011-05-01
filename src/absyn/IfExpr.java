package absyn;

public class IfExpr extends Expr {
    public Expr condition, thenClause, elseClause;
    
    public IfExpr(int pos, Expr condition, Expr thenClause, Expr elseClause) {
        super(pos);
        this.condition = condition;
        this.thenClause = thenClause;
        this.elseClause = elseClause;
    }

    public IfExpr(int pos, Expr condition, Expr thenClause) {
        this(pos, condition, thenClause, null);
    }
}
