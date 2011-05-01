package absyn;

public class SubscriptLValue extends LValue {
    public LValue lvalue;
    public Expr expr;

    public SubscriptLValue(int pos, LValue lvalue, Expr expr) {
        super(pos);
        this.lvalue = lvalue;
        this.expr = expr;
    }
}

