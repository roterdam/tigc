package absyn;

import symbol.Symbol;

public class TypeField extends Absyn {
    public Symbol name, type;

    public TypeField(int pos, Symbol name, Symbol type) {
        super(pos);
        this.name = name;
        this.type = type;
    }
}

