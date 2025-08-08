package org.example.nodes;

import org.example.events.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RawInput extends Node {

    private final String event;
    private Event<Object> input;

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

    public Response give(Event<Object> input) {
        var filtered = input.filter(accepts());
        if (!filtered.getDataSet().isEmpty()) {
            var resp = new Response(Optional.empty(), supply(filtered));
            if (input.getTypes().contains(this.event)) {
                return trigger(input.timestamp()).merge(resp);
            }
        }
        return Response.empty();
    }

    @Override
    protected List<Timer> supply(Event<Object> input) {
        if (input.getTypes().contains(this.event)) {
            this.input = new Event<>(event, input.data().get(event), input.timestamp());
        }
        return List.of();
    }

    @Override
    protected Response trigger(long timestamp) {
        if (input != null && timestamp == input.timestamp()) {
            return new Response(Optional.of(input), List.of());
        } else {
            return Response.empty();
        }
    }

}
