package absyn;

import symbol.Symbol;

public class VarLValue extends LValue {
    public Symbol name;

    public VarLValue(int pos, Symbol name) {
        super(pos);
        this.name = name;
    }
}

