package semant;

import intermediate.*;

class TranslateResult {
    type.Type type;
    IntermediateCodeList ic;

    public TranslateResult(IntermediateCodeList ic, type.Type type) {
        this.ic = ic;
        this.type = type;
    }
}

