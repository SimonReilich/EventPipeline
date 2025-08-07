package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class Group extends Node {

    private final Node driving;
    private final long tolerance;
    private final Node[] other;
    private final Map<String, Event<?>> values;

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
    protected List<Node> children() {
        List<Node> children = new ArrayList<>();
        children.add(driving);
        children.addAll(Arrays.asList(other));
        return children;
    }

    @Override
    public String getOutputSignalName() {
        return "Group(" +
                driving.getOutputSignalName() + ", " +
                Arrays.stream(other).map(Node::getOutputSignalName).collect(Collectors.joining(", ")) +
                ")[" + this.hashCode() + "]";
    }

    @Override
    protected void supply(Event<?> input) {
        Main.logEventSupplied(input);

        Arrays.stream(other)
                .filter(node -> node.accepts().contains(input.getName()))
                .forEach(node -> {
                    node.supply(input);
                    values.put(node.getOutputSignalName(), input);
                });

        if (driving.accepts().contains(input.getName())) {
            driving.supply(input);
        }

        for (var entry : values.entrySet()) {
            if (entry.getValue().getTimestamp() + tolerance < input.getTimestamp()) {
                values.remove(entry.getKey());
            }
        }
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {

        if (driving.accepts().contains(input.getName())) {
            var outputDriving = driving.trigger(input);
            if (outputDriving.isPresent()) {
                Arrays.stream(other)
                        .filter(node -> !(node instanceof Window))
                        .map(n -> n.trigger(values.getOrDefault(n.getOutputSignalName(), new Event<>(
                                Integer.toString(this.hashCode()),
                                null,
                                input.getTimestamp()
                        ))))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(e -> values.put(e.getName(), e));

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
                        .forEach(e -> values.put(e.getName(), e));

                values.put(outputDriving.get().getName(), outputDriving.get());

                Optional<Event<?>> result = Optional.of(new Event<>(
                        getOutputSignalName(),
                        values.entrySet().stream()
                                .map(e -> Map.entry(e.getKey(), e.getValue().getData()))
                                .toArray(),
                        input.getTimestamp()
                ));
                values.clear();
                result.ifPresent(Main::logEventTriggerd);
                return result;
            }
        }

        return Optional.empty();
    }

}
