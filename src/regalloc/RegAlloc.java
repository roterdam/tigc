package regalloc;

import java.util.*;
import util.Graph;
import intermediate.Temp;

public class RegAlloc {
    Map<Temp, Register> map = null;
    Set<Temp> spills = new HashSet<Temp>();
    Set<Temp> candidates = null;
    Graph<Temp> g;
    ArrayList<Register> regs;

    public RegAlloc(Graph<Temp> interfereGraph, ArrayList<Register> regs,
            Map<Temp, Register> preAlloc, Set<Temp> spillCandidates) {
        if (preAlloc == null)
            map = new HashMap<Temp, Register>();
        else
            map = preAlloc;
        candidates = spillCandidates;
        g = interfereGraph;
        this.regs = regs;
    }

    public Set<Temp> getSpill() {
        return spills;
    }

    public Map<Temp, Register> getMap() {
        return map;
    }

    public Register get(Temp t) {
        return map.get(t);
    }

    public boolean color() {
        int k = regs.size();

        Temp kick = null;
        Set<Temp> neighbour = null;

        boolean flag = false;
        for (Temp t: g.nodes()) {
            if (map.containsKey(t))
                continue;
            flag = true;
            if (!candidates.contains(t))
                continue;
            if (kick == null) {
                kick = t;
                neighbour = new HashSet<Temp>(g.succ(t));
            }
            if (g.inDegree(t) < k) {
                kick = t;
                neighbour = new HashSet<Temp>(g.succ(t));
                break;
            }
        }

        if (kick == null)
            return !flag;

        g.removeNode(kick);
        if (!color())
            return false;

        Set<Register> candidates = new HashSet<Register>(regs);
        for (Temp t: neighbour)
            if (map.containsKey(t))
                candidates.remove(map.get(t));

        Register r = null;
        for (Register rt: candidates) {
            r = rt;
            break;
        }

        if (r == null)
            spills.add(kick);
        else
            map.put(kick, r);

        return true;
    }
}

