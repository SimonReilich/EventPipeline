package org.example.nodes;

import org.example.events.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Wrap extends Node {

    private final String name;
    private final Node node;

    public Wrap(String name, Node node) {
        this.name = name;
        this.node = node;
    }

    @Override
    public Set<String> accepts() {
        return node.accepts();
    }

    @Override
    public Set<String> requires() {
        return node.requires();
    }

    @Override
    protected List<Node> children() {
        return List.of(node);
    }

    @Override
    protected void supply(Event<Object> input) {
        node.supply(input);
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        var nodeRes = node.trigger(input);
        return nodeRes.map(objectEvent -> new Event<>(
                name,
                objectEvent.data(),
                objectEvent.timestamp()
        ));
    }
}
