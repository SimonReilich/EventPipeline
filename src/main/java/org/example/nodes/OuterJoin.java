package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class OuterJoin extends Node {

    private final Node driving;
    private final Node[] other;
    private final Map<String, Object> values;

    public OuterJoin(Node driving, Node... other) {
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
    protected List<Timer> supply(Event<Object> input) {
        Main.logEventSupplied(input);
        var timers = new ArrayList<Timer>();
        Arrays.stream(other)
                .filter(node -> node.acceptsAny(input.getTypes()))
                .forEach(node -> timers.addAll(node.supply(input.filter(node.accepts()))));

        if (driving.acceptsAny(input.getTypes())) {
            driving.supply(input.filter(driving.accepts()));
        }
        return timers;
    }

    @Override
    protected Response trigger(Event<Object> input) {
        var timers = new ArrayList<Timer>();

        values.putAll(Arrays.stream(other)
                .map(n -> n.trigger(input))
                .peek(r -> timers.addAll(r.timers()))
                .filter(r -> r.event().isPresent())
                .flatMap(r -> r.event().get().data().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2)));

        var outputDriving = driving.trigger(input);
        timers.addAll(outputDriving.timers());
        outputDriving.event().ifPresent(d -> {
            values.clear();
            values.putAll(d.data());
        });

        Optional<Event<Object>> result = Optional.of(new Event<>(
                "oj",
                values.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                input.timestamp()
        ));
        result.ifPresent(Main::logEventSupplied);
        return new Response(result, timers);
    }
}
