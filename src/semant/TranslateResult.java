package semant;

import intermediate.*;
import java.util.HashSet;

class TranslateResult {
    type.Type type;
    IntermediateCodeList ic;
    HashSet<symbol.Symbol> foreigns;

    public TranslateResult(IntermediateCodeList ic, type.Type type, HashSet<symbol.Symbol> foreigns) {
        this.type = type;
        this.ic = ic;
        this.foreigns = foreigns;
    }

    public TranslateResult(IntermediateCodeList ic, type.Type type) {
        this(ic, type, new HashSet<symbol.Symbol>());
    }
}

