package absyn;

import symbol.Symbol;

public class CallExpr extends Expr {
    public Symbol func;
    public ExprList args;

    public CallExpr(int pos, Symbol func, ExprList args) {
        super(pos);
        this.func = func;
        this.args = args;
    }
}

