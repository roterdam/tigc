package util;

public class GraphNode<T> {
    public T value;

    public GraphNode(T x) {
        value = x;
    }

    public boolean equals(Object o) {
        return this == o;
    }
}

