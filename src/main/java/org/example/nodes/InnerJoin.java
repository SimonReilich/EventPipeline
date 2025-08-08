package org.example.nodes;

import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class InnerJoin extends Node {

    private final Node driving;
    private final Node[] other;
    private final Map<String, Object> values;
    private static long nextId = 0;

    private static long newId() {
        return nextId++;
    }

    public InnerJoin(Node driving, Node... other) {
        super(newId());
        this.driving = driving;
        this.other = other;
        this.values = new HashMap<>();
    }

    public String name() {
        return "ij" + id;
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
    protected List<Timer> supply(Event<Object> input) {
        var timers = new ArrayList<Timer>();
        Arrays.stream(other)
                .filter(node -> node.acceptsAny(input.getTypes()))
                .forEach(node -> timers.addAll(node.supply(input.filter(node.accepts()))));

        if (driving.acceptsAny(input.getTypes())) {
            timers.addAll(driving.supply(input.filter(driving.accepts())));
        }
        return timers;
    }

    @Override
    protected Response trigger(long timestamp) {
        var timers = new ArrayList<Timer>();

        values.putAll(Arrays.stream(other)
                .map(n -> n.trigger(timestamp))
                .peek(r -> timers.addAll(r.timers()))
                .filter(r -> r.event().isPresent())
                .map(r -> r.event().get())
                .flatMap(e -> e.data().entrySet().stream())
                .peek(e -> values.remove(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2)));

        var outputDriving = driving.trigger(timestamp);
        timers.addAll(outputDriving.timers());
        outputDriving.event().ifPresent(d -> {
            values.clear();
            values.putAll(d.data());
        });

        if (children().stream()
                .allMatch(node -> node.requires().stream().allMatch(values::containsKey))
                && driving.requires().stream().allMatch(values::containsKey)
        ) {
            Optional<Event<Object>> result = Optional.of(new Event<>(
                    "ij" + id,
                    values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    timestamp
            ));
            return new Response(result, timers);
        }

        return Response.empty();
    }

}
