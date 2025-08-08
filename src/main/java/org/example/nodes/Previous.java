package org.example.nodes;

import org.example.events.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Previous extends Node {

    private static long nextId = 0;
    private final Node node;
    private Event<Object> saved;
    private Event<Object> current;

    public Previous(Node node) {
        super(newId());
        this.node = node;
    }

    private static long newId() {
        return nextId++;
    }

    public String name() {
        return "prv" + id;
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
                    "prv" + id,
                    temp.data(),
                    timestamp
            ));
            return new Response(result, List.of());
        }
        return Response.empty();
    }

}
