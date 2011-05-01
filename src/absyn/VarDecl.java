package absyn;

import symbol.Symbol;

public class VarDecl extends Decl {
    public Symbol id, type;
    public Expr value;

    public VarDecl(int pos, Symbol id, Symbol type, Expr value) {
        super(pos);
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public VarDecl(int pos, Symbol id, Expr value) {
        this(pos, id, null, value);
    }
}

