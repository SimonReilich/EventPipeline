package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class RawInput extends Node {

    private final String event;

    public RawInput(String event) {
        this.event = event;
    }

    @Override
    public Set<String> accepts() {
        return Set.of(event);
    }

    @Override
    protected List<Node> children() {
        return List.of();
    }

    @Override
    public String getOutputSignalName() {
        return event;
    }

    @Override
    protected void supply(Event<?> input) {
        Main.logEventSupplied(input);
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {
        if (Objects.equals(input.getName(), event)) {
            Main.logEventTriggerd(input);
            return Optional.of(input);
        }
        return Optional.empty();
    }

}
