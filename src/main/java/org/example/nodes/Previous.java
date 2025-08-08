package org.example.nodes;

import org.example.events.Event;

import java.util.*;

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
    protected List<Timer> supply(Event<Object> input) {
        var res = node.give(input);
        res.event().ifPresent(event -> current = event);
        return res.timers();
    }

    @Override
    protected Response trigger(long timestamp) {
        var temp = saved;
        saved = current;
        current = null;
        if (temp != null) {
            Optional<Event<Object>> result = Optional.of(new Event<>(
                    "prv",
                    temp.data(),
                    timestamp
            ));
            return new Response(result, List.of());
        }
        return Response.empty();
    }

}
