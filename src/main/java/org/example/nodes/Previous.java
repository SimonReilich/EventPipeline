package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;

public class Previous extends Node {

    private final Node node;
    private Event<?> saved;
    private Event<?> current;

    public Previous(Node node) {
        super();
        this.node = node;
    }

    @Override
    protected Set<String> accepts() {
        return new HashSet<>(node.accepts());
    }

    @Override
    protected List<Node> children() {
        return List.of(node);
    }

    @Override
    public String getOutputSignalName() {
        return "Previous(" + node.getOutputSignalName() + ")[" + this.hashCode() + "]";
    }

    @Override
    protected void supply(Event<?> input) {
        var res = node.give(input);
        res.ifPresent(event -> current = event);
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {
        var temp = saved;
        saved = current;
        current = null;
            if (temp != null) {
                Optional<Event<?>> result = Optional.of(new Event<>(
                        getOutputSignalName(),
                        temp.getData(),
                        input.getTimestamp())
                );
                result.ifPresent(Main::logEvent);
                return result;
            }
            return Optional.empty();
    }
}
