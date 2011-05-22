package semant;

import intermediate.*;

class TranslateResult {
    type.Type type;
    IntermediateCodeList codes;
    Access place;

    public TranslateResult(IntermediateCodeList ic, type.Type type, Access place) {
        this.codes = ic;
        this.type = type;
        this.place = place;
    }

    public TranslateResult(IntermediateCodeList ic, type.Type type) {
        this(ic, type, null);
    }
}

