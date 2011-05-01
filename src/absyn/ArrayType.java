package absyn;

import symbol.Symbol;

public class ArrayType extends Type {
    public Symbol base;

    public ArrayType(int pos, Symbol base) {
        super(pos);
        this.base = base;
    }
}

