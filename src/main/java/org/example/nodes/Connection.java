package org.example.nodes;

public class Connection<T> {
    private final T value;

    private Connection(T value) {
        assert (value instanceof Node || value instanceof String);
        this.value = value;
    }

    public static Connection<String> s(String string) {
        return new Connection<>(string);
    }

    public static Connection<Node> n(Node node) {
        return new Connection<>(node);
    }

    public T value() {
        return this.value;
    }
}