package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class InnerJoin extends Node {

    private final Node driving;
    private final Node[] other;
    private final Map<String, Object> values;

    public InnerJoin(Node driving, Node... other) {
        this.driving = driving;
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
        Main.logEventSupplied(input, "InnerJoin");
        Arrays.stream(other)
                .filter(node -> node.acceptsAny(input.getTypes()))
                .forEach(node -> node.supply(input.filter(node.accepts())));

        if (driving.acceptsAny(input.getTypes())) {
            driving.supply(input.filter(driving.accepts()));
        }
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        values.putAll(Arrays.stream(other)
                .flatMap(n -> n.trigger(input).orElse(new Event<>("", Map.of(), 0)).data().entrySet().stream())
                .peek(e -> values.remove(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2)));

        var outputDriving = driving.trigger(input);
        outputDriving.ifPresent(d -> {
            values.clear();
            values.putAll(d.data());
        });

        if (children().stream()
                .allMatch(node -> node.requires().stream().allMatch(values::containsKey))
                && driving.requires().stream().allMatch(values::containsKey)
        ) {
            Optional<Event<Object>> result = Optional.of(new Event<>(
                    values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    input.timestamp()
            ));
            result.ifPresent(r -> Main.logEventTriggerd(r, "InnerJoin"));
            return result;
        }

        return Optional.empty();
    }

}
