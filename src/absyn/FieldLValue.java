package absyn;

import symbol.Symbol;

public class FieldLValue extends LValue {
    public LValue lvalue;
    public Symbol id;

    public FieldLValue(int pos, LValue lvalue, Symbol id) {
        super(pos);

        this.lvalue = lvalue;
        this.id = id;
    }
}

