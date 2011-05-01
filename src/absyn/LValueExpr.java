package absyn;

public class LValueExpr extends Expr {
    public LValue lvalue;

    public LValueExpr(int pos, LValue lvalue) {
        super(pos);
        this.lvalue = lvalue;
    }
}

