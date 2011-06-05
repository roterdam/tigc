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

        Temp kick = null, any = null;
        Set<Temp> neighbour = null, anyNeighbour = null;

        boolean finish = true;
        for (Temp t: g.nodes()) {
            if (map.containsKey(t))
                continue;
            finish = false;
            if (candidates.contains(t)) {
                any = t;
                anyNeighbour = new HashSet<Temp>(g.succ(t));
            }
            if (g.inDegree(t) < k) {
                kick = t;
                neighbour = new HashSet<Temp>(g.succ(t));
                break;
            }
        }

        if (finish)
            return true;

        if (kick == null) {
            kick = any;
            neighbour = anyNeighbour;
        }

        if (kick == null)
            return false;

        g.removeNode(kick);
        if (!color())
            return false;

        Set<Register> colorCandidates = new HashSet<Register>(regs);
        for (Temp t: neighbour) {
            if (map.containsKey(t)) {
                colorCandidates.remove(map.get(t));
//                if (colorCandidates.isEmpty())
//                    break;
            }
        }

        Register r = null;
        for (Register rt: colorCandidates) {
            r = rt;
            break;
        }

        if (r == null) {
            if (candidates.contains(kick))
                spills.add(kick);
            else
                return false;
        }
        else
            map.put(kick, r);

        return true;
    }
}

