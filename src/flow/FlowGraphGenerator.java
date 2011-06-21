package flow;

import arch.InstructionList;

public abstract class FlowGraphGenerator {
    public abstract FlowGraph build(InstructionList list);
}

