package absyn;

public class WhileExpr extends Expr {
    public Expr condition, body;

    public WhileExpr(int pos, Expr condition, Expr body) {
        super(pos);
        this.condition = condition;
        this.body = body;
    }
}

