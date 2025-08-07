package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class CombineLatest extends Node {

    private final Node[] children;

    public CombineLatest(Node... children) {
        this.children = children;
        this.values = new HashMap<>();
    }

    @Override
    protected Set<String> accepts() {
        return children().stream().map(Node::getOutputSignalName).collect(Collectors.toSet());
    }

    @Override
    protected List<Node> children() {
        return Arrays.stream(children).toList();
    }

    @Override
    public String getOutputSignalName() {
        return "CombineLatest(" +
                Arrays.stream(children).map(Node::getOutputSignalName).collect(Collectors.joining(", ")) +
                ")[" + this.hashCode() + "]";
    }

    private final Map<String, Object> values;

    @Override
    protected void supply(Event<?> input) {
        Arrays.stream(children)
                .map(c -> c.give(input))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(e -> values.put(e.getName(), e.getData()));
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {
        Optional<Event<?>> result = Optional.of(
                new Event<>(
                        getOutputSignalName(),
                        values.entrySet().toArray(),
                        input.getTimestamp()
                )
        );
        result.ifPresent(Main::logEvent);
        return result;
    }
}
