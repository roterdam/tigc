package flow;

import arch.InstructionList;

public abstract class InstructionRewriter {
    public abstract InstructionList rewrite(FlowGraph g);
}
