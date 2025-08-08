package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Prefix extends Node {

    private final String prefix;
    private final Node node;

    public Prefix(String prefix, Node node) {
        this.prefix = prefix;
        this.node = node;
    }

    @Override
    public Set<String> accepts() {
        return node.accepts();
    }

    @Override
    public Set<String> requires() {
        return node.requires();
    }

    @Override
    protected List<Node> children() {
        return List.of(node);
    }

    @Override
    protected void supply(Event<Object> input) {
        Main.logEventSupplied(input, "Prefix");
        node.supply(input);
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        Optional<Event<Object>> nodeRes = node.trigger(input);
        var result = nodeRes.map(objectEvent -> new Event<>(
                objectEvent.getDataSet().stream()
                        .map(e -> Map.entry(prefix + e.getKey(), e.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                objectEvent.getTimestamp()
        ));
        result.ifPresent(r -> Main.logEventTriggerd(r, "Prefix"));
        return result;
    }
}
