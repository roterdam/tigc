package util;

import java.util.*;

public class Graph<NodeType> {
    HashSet<NodeType> nodes = new HashSet<NodeType>();
    HashMap<NodeType, HashSet<NodeType>> preds = new HashMap<NodeType, HashSet<NodeType>>(),
        succs = new HashMap<NodeType, HashSet<NodeType>>();

    public Graph() {
    }

    public boolean inGraph(NodeType n) {
        return nodes.contains(n);
    }

    public void addNode(NodeType n) {
        nodes.add(n);
    }

    public void addDirectedEdge(NodeType u, NodeType v) {
        if (!inGraph(u))
            addNode(u);
        if (!inGraph(v))
            addNode(v);
        if (!succs.containsKey(u))
            succs.put(u, new HashSet<NodeType>());
        succs.get(u).add(v);
        if (!preds.containsKey(v))
            preds.put(v, new HashSet<NodeType>());
        preds.get(v).add(u);
    }

    public void addEdge(NodeType u, NodeType v) {
        addDirectedEdge(u, v);
    }

    public void addUndirectedEdge(NodeType u, NodeType v) {
        addDirectedEdge(u, v);
        addDirectedEdge(v, u);
    }

    public void removeDirectedEdge(NodeType u, NodeType v) {
        if (succs.containsKey(u)) {
            succs.get(u).remove(v);
            preds.get(v).remove(u);
        }
    }

    public void removeUndirectedEdge(NodeType u, NodeType v) {
        removeDirectedEdge(u, v);
        removeDirectedEdge(v, u);
    }

    public void removeNode(NodeType n) {
        HashSet<NodeType> t = succs.get(n);
        if (t != null) {
            t = new HashSet<NodeType>(t);
            for (NodeType v: t)
                removeDirectedEdge(n, v);
            succs.remove(n);
        }
        t = preds.get(n);
        if (t != null) {
            t = new HashSet<NodeType>(t);
            for (NodeType u: t)
                removeDirectedEdge(u, n);
            preds.remove(n);
        }
        nodes.remove(n);
    }

    public boolean isDirectedEdge(NodeType u, NodeType v) {
        HashSet<NodeType> t = succs.get(u);
        if (t == null)
            return false;
        return t.contains(v);
    }

    public boolean isEdge(NodeType u, NodeType v) {
        return isDirectedEdge(u, v);
    }

    public Set<NodeType> pred(NodeType n) {
        Set<NodeType> ret = preds.get(n);
        if (ret == null)
            return new HashSet<NodeType>();
        else
            return ret;
    }

    public Set<NodeType> succ(NodeType n) {
        Set<NodeType> ret = succs.get(n);
        if (ret == null)
            return new HashSet<NodeType>();
        else
            return ret;
    }

    public int inDegree(NodeType n) {
        return pred(n).size();
    }

    public int outDegree(NodeType n) {
        return succ(n).size();
    }

    Set<NodeType> visited;
    boolean isPath(NodeType from, NodeType to) {
        if (visited.contains(from))
            return false;
        visited.add(from);
        if (from.equals(to))
            return true;
        Set<NodeType> succs = succ(from);
        for (NodeType n: succs) {
            if (isPath(n, to))
                return true;
        }
        return false;
    }

    public boolean isLoopEdge(NodeType u, NodeType v) {
        if (!isEdge(u, v))
            return false;
        visited = new HashSet<NodeType>();
        return isPath(v, u);
    }
}

