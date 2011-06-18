package semant;

import intermediate.*;

class TranslateResult {
    type.Type type;
    IntermediateCodeList codes;
    Access place;
    
    Integer c;
    int loopCount = 0;

    public TranslateResult(IntermediateCodeList ic, type.Type type, Access place, Integer c, int loopCount) {
        this.codes = ic;
        this.type = type;
        this.place = place;
        this.c = c;
        this.loopCount = loopCount;
    }

    public TranslateResult(IntermediateCodeList ic, type.Type type, int loopCount) {
        this(ic, type, null, null, loopCount);
    }
}

