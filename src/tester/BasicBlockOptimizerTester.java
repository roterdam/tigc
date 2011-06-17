package tester;

import optimization.BasicBlockOptimizer;
import mips32.Instruction;
import mips32.InstructionGenerator;
import flow.BasicBlock;
import intermediate.Temp;
import flow.LifeAnalysis;
import flow.FlowGraph;
import frame.Frame;
import arch.Const;
import intermediate.Label;

public class BasicBlockOptimizerTester {
    static void print(BasicBlock block) {
        for (arch.Instruction i: block)
            System.out.println(i.toString());
    }

    public static void main(String[] args) {
        Frame frame = new Frame(null, null, true);
        Temp a = frame.addLocal(), b = frame.addLocal(), c = frame.addLocal(),
             d = frame.addLocal(), e = frame.addLocal(), f = frame.addLocal(), i = frame.addLocal();

        BasicBlock block = new BasicBlock();
        block.add(Instruction.MOVE(frame, a, a));

        System.out.println("BEFORE:");
        print(block);

        BasicBlock end = new BasicBlock();
        end.add(Instruction.ADD(frame, a, a, a));

        FlowGraph g = new FlowGraph();
        g.addEdge(block, end, false);

        LifeAnalysis life = new LifeAnalysis(g);
        System.out.println("LIFE:");
        for (Temp t: life.out(block))
            System.out.println(t.toString());
        BasicBlockOptimizer opt = new BasicBlockOptimizer(block, life, new InstructionGenerator());
        opt.optimize();

        System.out.println("AFTER:");
        print(block);
    }
}

