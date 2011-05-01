package absyn;

import symbol.Symbol;

public class ForExpr extends Expr {
    public Symbol var;
    public Expr begin, end, body;

    public ForExpr(int pos, Symbol var, Expr begin, Expr end, Expr body) {
        super(pos);
        this.var = var;
        this.begin = begin;
        this.end = end;
        this.body = body;
    }
}

