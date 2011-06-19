package optimization;

import arch.Instruction;
import intermediate.Temp;
import flow.FlowGraph;
import flow.DominatingSet;
import flow.BasicBlock;
import java.util.*;

public class LoopInvariantCodeMotion {
    FlowGraph flow;

    public LoopInvariantCodeMotion(FlowGraph flow) {
        this.flow = flow;
    }

    public FlowGraph optimize() {
        DominatingSet dom = new DominatingSet(flow);

        return flow;
    }
}
