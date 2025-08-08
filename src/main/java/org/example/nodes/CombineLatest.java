package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class CombineLatest extends Node {

    private final Node[] children;
    private final Map<String, Object> values;

    public CombineLatest(Node... children) {
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
    protected void supply(Event<Object> input) {
        Main.logEventSupplied(input, "CombineLatest");
        Arrays.stream(children)
                .map(c -> c.give(input))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(e -> values.putAll(e.getDataSet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        Optional<Event<Object>> result = Optional.of(
                new Event<>(
                        values,
                        input.getTimestamp()
                )
        );
        result.ifPresent(r -> Main.logEventTriggerd(r, "CombineLatest"));
        return result;
    }

}
