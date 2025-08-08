package org.example.nodes;

import org.example.events.Event;

import java.util.*;

public class Delay extends Node {

    private static long nextId = 0;
    private final long delay;
    private final Node node;
    private final Queue<Response> saved;

    public Delay(long delay, Node node) {
        super(newId());
        this.delay = delay;
        this.node = node;
        this.saved = new LinkedList<>();
    }

    private static long newId() {
        return nextId++;
    }

    public String name() {
        return "del" + delay;
    }

    @Override
    public Set<String> accepts() {
        Set<String> accepts = new HashSet<>();
        accepts.add("Synthetic" + this.hashCode());
        accepts.addAll(node.accepts());
        return accepts;
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
    public Response give(Event<Object> input) {
        var filtered = input.filter(accepts());
        if (!filtered.getDataSet().isEmpty()) {
            var resp = new Response(Optional.empty(), supply(filtered));
            if (input.getTypes().contains("Synthetic" + this.hashCode())) {
                return trigger(input.timestamp()).merge(resp);
            } else {
                return resp;
            }
        } else {
            return Response.empty();
        }
    }

    @Override
    protected List<Timer> supply(Event<Object> input) {
        saved.add(node.give(input));
        return List.of(new Timer(input.timestamp() + this.delay, this.hashCode()));
    }

    @Override
    protected Response trigger(long timestamp) {
        if (!saved.isEmpty() && saved.peek() != null && saved.peek().event().isPresent()) {
            Optional<Event<Object>> result = Optional.of(new Event<>(
                    "del" + id,
                    saved.poll().event().get().data(),
                    timestamp)
            );
            return new Response(result, List.of());
        }
        return Response.empty();
    }
}
