package org.example.nodes;

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
    public Response give(Event<Object> input) {
        var filtered = input.filter(accepts());
        if (!filtered.getDataSet().isEmpty()) {
            var resp = new Response(Optional.empty(), supply(filtered));
            if (driving.accepts().stream().anyMatch(t -> input.getTypes().contains(t))) {
                return trigger(input.timestamp()).merge(resp);
            }
            return resp;
        } else {
            return Response.empty();
        }
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
        List<Timer> timers = new ArrayList<>();

        Arrays.stream(other)
                .forEach(node -> {
                    if (!input.filter(node.accepts()).data().isEmpty()) {
                        timers.addAll(node.supply(input.filter(node.accepts())));

                        values.putAll(input.filter(node.accepts()).getDataSet().stream()
                                .map(e -> Map.entry(e.getKey(), Map.entry(e.getValue(), input.timestamp())))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    }
                });

        if (driving.acceptsAny(input.getTypes())) {
            timers.addAll(driving.supply(input.filter(driving.accepts())));
        }

        for (var entry : values.entrySet()) {
            if (entry.getValue().getValue() + tolerance < input.timestamp()) {
                values.remove(entry.getKey());
            }
        }

        return timers;
    }

    @Override
    protected Response trigger(long timestamp) {
        var outputDriving = driving.trigger(timestamp);
        var timers = outputDriving.timers();
        if (outputDriving.event().isPresent()) {
            Arrays.stream(other)
                    .filter(node -> !(node instanceof Window))
                    .map(node -> node.trigger(timestamp))
                    .peek(r -> timers.addAll(r.timers()))
                    .filter(r -> r.event().isPresent())
                    .map(r -> r.event().get())
                    .flatMap(e -> e.getDataSet().stream())
                    .forEach(e -> values.put(e.getKey(), Map.entry(e.getValue(), timestamp)));

            Arrays.stream(other)
                    .filter(node -> node instanceof Window)
                    .map(n -> n.trigger(-n.hashCode()))
                    .peek(r -> timers.addAll(r.timers()))
                    .filter(r -> r.event().isPresent())
                    .map(r -> r.event().get())
                    .flatMap(e -> e.getDataSet().stream())
                    .forEach(e -> values.put(e.getKey(), Map.entry(e.getValue(), timestamp)));

            outputDriving.event().ifPresent(event -> event.getDataSet().forEach(e -> values.put(e.getKey(), Map.entry(e.getValue(), timestamp))));

            Optional<Event<Object>> result = Optional.of(new Event<>(
                    "gr",
                    values.entrySet().stream()
                            .map(e -> Map.entry(e.getKey(), e.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getKey())),
                    timestamp
            ));
            values.clear();
            return new Response(result, timers);
        }
        return new Response(Optional.empty(), timers);
    }

}
