package intermediate;

import arch.*;

public class IR {
    public StringTable stringTable = new StringTable();
    public UnknownConstAccess wordLength = new UnknownConstAccess("WORD_LENGTH");
    public IntermediateCodeList codes = new IntermediateCodeList();

    public IR() {
    }
}

