package semant;

import intermediate.*;
import utils.SimpleLinkedList;

class TranslateResult {
    type.Type type;
    IntermediateCodeList ic;
    SimpleLinkedList<symbol.Symbol> foreigns;

    public TranslateResult(IntermediateCodeList ic, type.Type type, SimpleLinkedList<symbol.Symbol> foreigns) {
        this.type = type;
        this.ic = ic;
        this.foreigns = foreigns;
    }

    public TranslateResult(IntermediateCodeList ic, type.Type type) {
        this(ic, type, new SimpleLinkedList<symbol.Symbol>());
    }
}

