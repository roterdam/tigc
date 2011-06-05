package optimization;

import flow.BasicBlock;
import java.util.*;
import arch.Instruction;
import util.Graph;

public class BasicBlockOptimizer {
    BasicBlock block;
    Set<Temp> live;

    public BasicBlockOptimizer(BasicBlock block, Set<Temp> live) {
        this.block = block;
        this.live = live;
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
        int opCode;
        List<Node> preds;
        List<Instruction> list;
        List<VersionedTemp> useList;
        List<Temp> results;

        public Node(int opCode, List<VersionedTemp> useList) {
            this.opCode = opCode;
            this.useList = useList;
            this.list = new ArrayList<Instruction>();
            this.results = new ArrayList<Temp>();
            this.preds = new ArrayList<Node>();
        }
    }

    Map<Temp, VersionedTemp> map = new HashMap<Temp, VersionedTemp>();
    Map<VersionedTemp, Node> vnMap = new Map<VersionedTemp, Node>();

    List<Temp> use(Instruction ins) {
        List<Temp> ret = ins.use();
        if (ins.isLoad())
            ret.add(mem);
        return ret;
    }
    
    Temp def(Instruction ins) {
        Set<Temp> ret = ins.def();
        if (ins.isStore())
            ret.add(mem);
        if (ret.size() == 0)
            return null;
        else if (ret.size() == 1)
            return ret.get(0);
        else
            throw new Error("UNSUPPORTED: An instruction with multi temps defined");
    }

    VersionedTemp latestVersion(Temp t) {
        if (map.containsKey(t))
            return map.get(t);
        else {
            VersionedMap v = new VersionedMap(t, 0);
            map.put(t, v);
            return v;
        }
    }

    Node versionNode(VersionedTemp vt) {
        if (tnMap.containsKey(vt))
            return vnMap.get(vt);
        else {
            Node n = new Node(0, new ArrayList<VersionedTemp>());
            vnMap.put(vt, n);
            return n;
        }
    }

    VersionedTemp advanceVersion(Temp t) {
        if (map.containsKey(t)) {
            VersionedMap v = new VersionedMap(t, map.get(t).version + 1);
            map.put(t, v);
            return v;
        } else {
            VersionedMap v = new VersionedMap(t, 0);
            map.put(t, v);
            return v;
        }
    }

    void setResults(Graph g, Node n) {
        Instruction any = null;
        Iterator<Instruction> iter = n.list.iterator();
        while (iter.hasNext()) {
            Instruction i = iter.next();
            if (any == null)
                any = i;

            boolean dead = true;
            for (Temp t: i.def()) {
                if (live.contains(t) && versionNode(latestVersion(t)) == n) {
                    n.results.add(t);
                    dead = false;
                }
            }
            if (!i.hasSideEffects() && dead && (i.def().size() == 1 && !n.results.contains(i.def().get(0))))
                iter.remove();
        }
        if (n.results.isEmpty()) {
            n.list.add(any);
            if (t.def().size() > 0)
                n.results.add(t.def().at(0));
        }

    }

    void setResults(Graph g) {
        for (Node n: g.tails())
            setResults(g, n);
    }

    public BasicBlock optimize() {
        Instruction lastJump = block.list.getLast();
        if (lastJump != null && lastJump.isJump()) {
            live = new HashSet<Temp>(live);
            live.removeAll(lastJump.def());
            live.addAll(lastJump.use());
        } else
            lastJump = null;

        Graph<Node> g = new Graph<Node>();
        for (Instruction i: block.list) {
            if (i == lastJump)
                continue;

            List<VersionedTemp> versioned = new ArrayList<VersionedTemp>();
            for (Temp t: i.useList())
                versioned.add(latestVersion(t));

            Node n = null;
            if (!i.hasSideEffects()) {
                for (Node nn: g) {
                    if (nn.opCode == i.opCode() && nn.useList.equals(versioned)) {
                        n = nn;
                        break;
                    }
                }
            }
            if (n == null) {
                n = new Node(i.opCode(), versioned);
                n.list.add(i);
                g.addNode(n);

                for (VersionedTemp use: versioned) {
                    Node s = versionNode(use, n);
                    g.addEdge(s, n);
                    n.preds.add(s);
                }
            } else
                n.ins.add(i);

            for (Temp t: i.def()) {
                VersionedTemp vt = advanceVersion(t);
                vnMap.put(vt, n);
            }
        }

        boolean change = false;
        do {
            for (Node n: new HashSet<Node>(g.tails())) {
                Iterator<Instruction> iter = n.list.iterator();
                while (iter.hasNext()) {
                    Instruction i = iter.next();
                    if (i.hasSideEffects())
                        continue;

                    boolean dead = true;
                    for (Temp t: i.def()) {
                        if (live.contains(t)) {
                            dead = false;
                            break;
                        }
                    }
                    if (dead)
                        iter.remove();
                }
                if (n.list.isEmpty()) {
                    g.removeNode(n);
                    change = true;
                }
            }
        } while (change);

        BasicBlock ret = new BasicBlock();
        ret.labels = block.labels;

        for (Node n: new HashSet<Node>(g.heads())) {

        }

        if (lastJump != null)
            ret.list.add(lastJump);
    }
}

