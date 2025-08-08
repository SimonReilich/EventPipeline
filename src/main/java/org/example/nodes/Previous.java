package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Previous extends Node {

    private final Node node;
    private Event<Object> saved;
    private Event<Object> current;

    public Previous(Node node) {
        super();
        this.node = node;
    }

    @Override
    public Set<String> accepts() {
        return new HashSet<>(node.accepts());
    }

    @Override
    public Set<String> requires() {
        return new HashSet<>(node.requires());
    }

    @Override
    protected List<Node> children() {
        return List.of(node);
    }

    @Override
    protected void supply(Event<Object> input) {
        Main.logEventSupplied(input, "Previous");
        var res = node.give(input);
        res.ifPresent(event -> current = event);
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        var temp = saved;
        saved = current;
        current = null;
        if (temp != null) {
            Optional<Event<Object>> result = Optional.of(new Event<>(
                    temp.data(),
                    input.timestamp())
            );
            result.ifPresent(r -> Main.logEventTriggerd(r, "Previous"));
            return result;
        }
        return Optional.empty();
    }

}
