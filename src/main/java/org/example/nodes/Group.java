package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class Group extends Node {

    private final Node driving;
    private final long tolerance;
    private final Node[] other;
    private final Map<String, Map.Entry<Object, Long>> values;

    public Group(Node driving, Node... other) {
        this.driving = driving;
        this.tolerance = 0;
        this.other = other;
        this.values = new HashMap<>();
    }

    public Group(long tolerance, Node driving, Node... other) {
        this.driving = driving;
        this.tolerance = tolerance;
        this.other = other;
        this.values = new HashMap<>();
    }

    @Override
    public Set<String> accepts() {
        var accepts = new HashSet<>(driving.accepts());
        Arrays.stream(other).forEach(node -> accepts.addAll(node.accepts()));
        return accepts;
    }

    @Override
    public Set<String> requires() {
        var requires = new HashSet<>(driving.requires());
        Arrays.stream(other).forEach(node -> requires.addAll(node.requires()));
        return requires;
    }

    @Override
    protected List<Node> children() {
        List<Node> children = new ArrayList<>();
        children.add(driving);
        children.addAll(Arrays.asList(other));
        return children;
    }

    @Override
    protected void supply(Event<Object> input) {
        Main.logEventSupplied(input, "Group");

        Arrays.stream(other)
                .forEach(node -> {
                    if (!input.filter(node.accepts()).getData().isEmpty()) {
                        node.supply(input.filter(node.accepts()));

                        values.putAll(input.filter(node.accepts()).getDataSet().stream()
                                .map(e -> Map.entry(e.getKey(), Map.entry(e.getValue(), input.getTimestamp())))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    }
                });

        if (driving.acceptsAny(input.getTypes())) {
            driving.supply(input.filter(driving.accepts()));
        }

        for (var entry : values.entrySet()) {
            if (entry.getValue().getValue() + tolerance < input.getTimestamp()) {
                values.remove(entry.getKey());
            }
        }
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {

        if (driving.acceptsAny(input.getTypes())) {
            var outputDriving = driving.trigger(input.filter(driving.accepts()));
            if (outputDriving.isPresent()) {
                Arrays.stream(other)
                        .filter(node -> !(node instanceof Window))
                        .flatMap(node -> node.trigger(input.filter(node.accepts())).stream())
                        .flatMap(e -> e.getDataSet().stream())
                        .forEach(e -> values.put(e.getKey(), Map.entry(e.getValue(), input.getTimestamp())));

                Arrays.stream(other)
                        .filter(node -> node instanceof Window)
                        .map(n -> n.trigger(
                                new Event<>(
                                        "SyntheticG" + n.hashCode(),
                                        null,
                                        input.getTimestamp()
                                )
                        ))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(e -> e.getDataSet().stream())
                        .forEach(e -> values.put(e.getKey(), Map.entry(e.getValue(), input.getTimestamp())));

                outputDriving.ifPresent(event -> event.getDataSet().forEach(e -> values.put(e.getKey(), Map.entry(e.getValue(), input.getTimestamp()))));

                Optional<Event<Object>> result = Optional.of(new Event<>(
                        values.entrySet().stream()
                                .map(e -> Map.entry(e.getKey(), e.getValue()))
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getKey())),
                        input.getTimestamp()
                ));
                values.clear();
                result.ifPresent(r -> Main.logEventTriggerd(r, "Group"));
                return result;
            }
        }

        return Optional.empty();
    }

}
