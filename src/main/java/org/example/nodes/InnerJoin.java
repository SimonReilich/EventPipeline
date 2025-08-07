package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class InnerJoin extends Node {

    private final Node driving;
    private final Node[] other;

    public InnerJoin(Node driving, Node... other) {
        this.driving = driving;
        this.other = other;
        this.values = new HashMap<>();
    }

    @Override
    protected Set<String> accepts() {
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
        return "InnerJoin(" +
                driving.getOutputSignalName() + ", " +
                Arrays.stream(other).map(Node::getOutputSignalName).collect(Collectors.joining(", ")) +
                ")[" + this.hashCode() + "]";
    }

    private final Map<String, Object> values;

    @Override
    protected void supply(Event<?> input) {
        Arrays.stream(other)
                .filter(node -> node.accepts().contains(input.getName()))
                .forEach(node -> node.supply(input));

        if (driving.accepts().contains(input.getName())) {
            driving.supply(input);
        }
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {
        values.putAll(Arrays.stream(other)
                .map(n -> n.trigger(input))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Event::getName, Event::getData)));

        var outputDriving = driving.trigger(input);
        outputDriving.ifPresent(d -> {
            values.clear();
            values.put(d.getName(), d.getData());
        });

        if (children().stream()
                .allMatch(node -> values.containsKey(node.getOutputSignalName()))
                && values.containsKey(driving.getOutputSignalName())
        ) {
            Optional<Event<?>> result = Optional.of(new Event<>(
                    getOutputSignalName(),
                    values.entrySet().toArray(),
                    input.getTimestamp()
            ));
            result.ifPresent(Main::logEvent);
            return result;
        }

        return Optional.empty();
    }
}
