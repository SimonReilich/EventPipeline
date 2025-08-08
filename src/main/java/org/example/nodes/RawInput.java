package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.List;
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

    public Set<String> requires() {
        return Set.of(event);
    }

    @Override
    protected List<Node> children() {
        return List.of();
    }

    @Override
    protected List<Timer> supply(Event<Object> input) {
        return List.of();
    }

    @Override
    protected Response trigger(Event<Object> input) {
        if (input.getTypes().contains(event)) {
            return new Response(Optional.of(input), List.of());
        }
        return Response.empty();
    }

}
