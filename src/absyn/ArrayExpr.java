package absyn;

import symbol.Symbol;

public class ArrayExpr extends Expr {
    public Symbol type;
    public Expr size;
    public Expr init;

    public ArrayExpr(int pos, Symbol type, Expr size, Expr init) {
        super(pos);
        this.type = type;
        this.size = size;
        this.init = init;
    }
}

