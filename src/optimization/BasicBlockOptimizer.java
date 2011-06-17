package optimization;

import flow.BasicBlock;
import java.util.*;
import arch.Instruction;
import util.Graph;
import flow.LifeAnalysis;
import intermediate.*;
import arch.InstructionGenerator;
import frame.Frame;

public class BasicBlockOptimizer {
    BasicBlock block;
    LifeAnalysis life;
    InstructionGenerator gen;

    public BasicBlockOptimizer(BasicBlock block, LifeAnalysis life, InstructionGenerator gen) {
        this.block = block;
        this.life = life;
        this.gen = gen;
    }

    static final Temp mem = Temp.newTemp(null);

    static class VersionedTemp {
        int version;
        Temp temp;

        public VersionedTemp(int version, Temp temp) {
            this.version = version;
            this.temp = temp;
        }
    }

    static class Node {
        List<Node> useList;
        Temp result;
        LinkedList<Instruction> ins;
        LinkedList<Instruction> nIns;

        public Node(List<Node> useList) {
            this.useList = useList;
            this.result = null;
            this.ins = new LinkedList<Instruction>();
            this.nIns = new LinkedList<Instruction>();
        }
    }

    Map<Temp, VersionedTemp> map = new HashMap<Temp, VersionedTemp>();
    Map<VersionedTemp, Node> vnMap = new HashMap<VersionedTemp, Node>();
    Map<List<Node>, List<Node>> uMap = new HashMap<List<Node>, List<Node>>();

    void newMap() {
        map = new HashMap<Temp, VersionedTemp>();
        vnMap = new HashMap<VersionedTemp, Node>();
        uMap = new HashMap<List<Node>, List<Node>>();
    }

    List<Temp> use(Instruction ins) {
        List<Temp> ret = ins.useList();
        if (ins.isLoad() || ins.isStore())
            ret.add(mem);
        return ret;
    }
    
    Temp def(Instruction ins) {
        Set<Temp> ret = ins.def();
        if (ins.isStore())
            ret.add(mem);
        if (ret.size() != 1)
            return null;

        for (Temp t: ret)
            return t;
        return null;
    }

    Set<Temp> defSet(Instruction ins) {
        Set<Temp> ret = ins.def();
        if (ins.isStore())
            ret.add(mem);
        return ret;
    }

    Set<Temp> useSet(Instruction ins) {
        Set<Temp> ret = ins.use();
        if (ins.isLoad() || ins.isStore())
            ret.add(mem);
        return ret;
    }

    VersionedTemp latestVersion(Temp t) {
        if (map.containsKey(t))
            return map.get(t);
        else {
            VersionedTemp v = new VersionedTemp(0, t);
            map.put(t, v);
            return v;
        }
    }

    Node versionNode(VersionedTemp vt) {
        if (vnMap.containsKey(vt))
            return vnMap.get(vt);
        else {
            Node n = new Node(new ArrayList<Node>());
            n.result = vt.temp;
            vnMap.put(vt, n);
            return n;
        }
    }

    VersionedTemp advanceVersion(Temp t) {
        if (map.containsKey(t)) {
            VersionedTemp v = new VersionedTemp(map.get(t).version + 1, t);
            map.put(t, v);
            return v;
        } else {
            VersionedTemp v = new VersionedTemp(0, t);
            map.put(t, v);
            return v;
        }
    }

    void rewrite(BasicBlock ret, Graph<Node> dag, Set<Temp> out, Frame frame) {
        out = new HashSet<Temp>(out);
        out.add(mem);
        for (Temp t: out) {
            if (!map.containsKey(t))
                continue;
            VersionedTemp vt = map.get(t);
            if (!vnMap.containsKey(vt))
                continue;
            Node n = vnMap.get(vt);

            for (Instruction i: n.ins) {
                if (def(i) == t)
                    n.nIns.add(i);
            }
        }

        boolean change = false;
        do {
            change = false;
            List<Node> tails = new ArrayList<Node>(dag.tails());
            for (Node n: tails) {
                if (n.nIns.size() == 0) {
                    dag.removeNode(n);
                    change = true;
                }
            }
        } while (change);

        List<Node> sorted = dag.topologicalSort();
        for (Node n: sorted) {
            if (n.result != null && n.result != mem) {
                Temp t = frame.addLocal();
                ret.add(gen.MOVE(frame, t, n.result));
                n.result = t;
            }
        }
        for (Node n: sorted) {
            if (n.ins.size() == 0)
                continue;

            
            List<Temp> use = new ArrayList<Temp>();
            for (Node p: n.useList)
                use.add(p.result);

            if (n.result == null) {
                Instruction model = null;
                for (Instruction i: n.ins)
                    if (!i.isMove()) {
                        model = i;
                        break;
                    }

                if (n.nIns.size() > 0)
                    n.result = def(n.nIns.removeFirst());
                else
                    n.result = n.ins.peekFirst().frame.addLocal();
                ret.add(model.rewrite(n.result, use));
            }

            for (Instruction i: n.nIns)
                ret.add(i.rewriteMove(n.result));
        }
    }

    boolean sameType(Node n, Instruction i) {
        for (Instruction ii: n.ins)
            if (!ii.sameExceptTemps(i))
                return false;
        return true;
    }

    int copyPropagation(BasicBlock block, List<Integer> moveFrom, List<Integer> moveAfter) {
        Map<Temp, Instruction> alias = new HashMap<Temp, Instruction>();
        Map<Instruction, Map<Temp, Instruction>> reaching = new HashMap<Instruction, Map<Temp, Instruction>>();
        Map<Temp, Instruction> now = new HashMap<Temp, Instruction>();

        int count = 0;
        Integer index = new Integer(0);

        BasicBlock ret = new BasicBlock();
        for (Instruction i: block) {
            reaching.put(i, new HashMap<Temp, Instruction>(now));

            List<Temp> newUse = new ArrayList<Temp>();
            List<Integer> expect = new ArrayList<Integer>();
            for (Temp t: i.useList()) {
                Temp r = t;
                boolean success = false;
                while (alias.containsKey(r)) {
                    Instruction move = alias.get(r);
                    Temp candidate = move.useList().get(0);
                    Map<Temp, Instruction> then = reaching.get(move);

                    if ((!then.containsKey(candidate) && !now.containsKey(candidate)) ||
                            then.get(candidate) == now.get(candidate)) {
                        r = candidate;
                        success = true;
                    } else {
                        int state = 0;
                        Integer index2 = new Integer(0);
                        for (Instruction j: block) {
                            if (j == move && state == 0)
                                state = 1;
                            else if (state == 1) {
                                if (j == i) {
                                    state = 2;
                                    break;
                                } else if (def(j) == candidate)
                                    expect.add(index2);
                            }
                            index2 = new Integer(index2.intValue() + 1);
                        }
                        
                        break;
                    }
                }
                newUse.add(r);
                if (success)
                    ++count;
            }
            ret.add(i.rewrite(def(i), newUse));

            if (expect.size() == 1) {
                moveFrom.add(expect.get(0));
                moveAfter.add(index);
            }

            if (i.isMove())
                alias.put(def(i), i);
            else {
                for (Temp t: i.def())
                    alias.remove(t);
            }
            now.put(def(i), i);

            index = new Integer(index.intValue() + 1);
        }

        block.replace(ret);
        return count;
    }

    boolean codeMotion(BasicBlock block, int from, int after) {
        int index = 0, state = 0;
        Set<Temp> use1 = new HashSet<Temp>(),
            def1 = new HashSet<Temp>(),
            use2 = new HashSet<Temp>(),
            def2 = new HashSet<Temp>();
        for (Instruction i: block) {
            if (index == from) {
                if (i.hasSideEffects() || i.isJump())
                    return false;
                state = 1;
                use1.addAll(useSet(i));
                def1.addAll(defSet(i));
            } else if (state == 1) {
                if (i.isJump() || i.hasSideEffects())
                    return false;
                use2.addAll(useSet(i));
                def2.addAll(defSet(i));
                if (index == after) {
                    break;
                }
            }
            ++index;
        }

        Set<Temp> t = new HashSet<Temp>(use1);
        t.retainAll(def2);
        if (t.size() > 0)
            return false;
        t = new HashSet<Temp>(use2);
        t.retainAll(def1);
        if (t.size() > 0)
            return false;
        t = new HashSet<Temp>(def1);
        t.retainAll(def2);
        if (t.size() > 0)
            return false;

        index = 0;
        BasicBlock ret = new BasicBlock();
        Instruction own = null;
        for (Instruction i: block) {
            if (index < from)
                ret.add(i);
            else if (index == from)
                own = i;
            else if (index <= after) {
                ret.add(i);
                if (index == after)
                    ret.add(own);
            } else
                ret.add(i);
            ++index;
        }

        block.replace(ret);

        return true;
    }

    public void optimize() {
        BasicBlock ret = new BasicBlock();
        ret.labels = block.labels;

        Graph<Node> g = new Graph<Node>();
        Frame firstFrame = null;
        for (Iterator<Instruction> iter = block.iterator();
                iter.hasNext(); ) {
            Instruction i = iter.next();
            if (firstFrame == null)
                firstFrame = i.frame;

            Temp def = def(i);
            if (def == null || i.isJump() || i.hasSideEffects()) {
                rewrite(ret, g, life.in(i), firstFrame);
                ret.add(i);
                g = new Graph<Node>();
                newMap();
                firstFrame = null;
            } else {
                List<Node> useList = new ArrayList<Node>();
                for (Temp t: use(i))
                    useList.add(versionNode(latestVersion(t)));
                Node n = null;
                if (i.isMove())
                    n = versionNode(latestVersion(i.useList().get(0)));
                else if (uMap.containsKey(useList)) {
                    for (Node nn: uMap.get(useList))
                        if (sameType(nn, i)) {
                            n = nn;
                            break;
                        }
                }
                if (n == null) {
                    List<Node> actualUseList = new ArrayList<Node>();
                    for (Temp t: i.useList())
                        actualUseList.add(versionNode(latestVersion(t)));
                    n = new Node(actualUseList);

                    if (!uMap.containsKey(useList))
                        uMap.put(useList, new ArrayList<Node>());
                    uMap.get(useList).add(n);

                    g.addNode(n);
                    for (Node nn: useList)
                        g.addEdge(nn, n);
                } else
                    g.addNode(n);

                n.ins.add(i);
                vnMap.put(advanceVersion(def), n);

                if (!iter.hasNext())
                    rewrite(ret, g, life.out(i), firstFrame);
            }
        }
        do {
            ArrayList<Integer> moveFrom = new ArrayList<Integer>(),
                moveAfter = new ArrayList<Integer>();
            copyPropagation(ret, moveFrom, moveAfter);
            if (moveFrom.size() == 0)
                break;
            else {
                boolean restart = false;
                for (int i = 0; i < moveFrom.size(); ++i) {
                    if (codeMotion(ret, moveFrom.get(i).intValue(), moveAfter.get(i).intValue())) {
                        restart = true;
                        break;
                    }
                }
                if (restart)
                    continue;
                else
                    break;
            }
        } while (true);
        block.replace(ret);

        ret = new BasicBlock();
        for (Instruction i: block)
            if (!(i.isMove() && i.useList().get(0) == def(i)))
                ret.add(i);
        block.replace(ret);
    }
}

