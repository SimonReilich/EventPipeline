package org.example.nodes;

import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class CombineLatest extends Node {

    private final Node[] children;
    private final Map<String, Object> values;
    private static long nextId = 0;

    private static long newId() {
        return nextId++;
    }

    public CombineLatest(Node... children) {
        super(newId());
        this.children = children;
        this.values = new HashMap<>();
    }

    @Override
    public Set<String> accepts() {
        return children().stream().flatMap(n -> n.accepts().stream()).collect(Collectors.toSet());
    }

    @Override
    public Set<String> requires() {
        return children().stream().flatMap(n -> n.requires().stream()).collect(Collectors.toSet());
    }

    @Override
    protected List<Node> children() {
        return Arrays.stream(children).toList();
    }

    @Override
    protected List<Timer> supply(Event<Object> input) {
        return Arrays.stream(children)
                .map(c -> c.give(input))
                .filter(r -> r.event().isPresent())
                .map(Response::save)
                .peek(r -> {
                    values.putAll(r.event().getDataSet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }).flatMap(r -> r.timers().stream()).toList();
    }

    @Override
    protected Response trigger(long timestamp) {
        Optional<Event<Object>> result = Optional.of(
                new Event<>(
                        "cl" + id,
                        values,
                        timestamp
                )
        );
        return new Response(result, List.of());
    }

}
