package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;

public class Delay extends Node {

    private final long delay;
    private final Node node;
    private final Queue<Response> saved;

    public Delay(long delay, Node node) {
        super();
        this.delay = delay;
        this.node = node;
        this.saved = new LinkedList<>();
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
    protected List<Timer> supply(Event<Object> input) {
        Main.logEventSupplied(input);
        saved.add(node.give(input));
        return List.of(new Timer(input.timestamp() + this.delay, this.hashCode()));
    }

    @Override
    protected Response trigger(Event<Object> input) {
        if (input.getTypes().contains("Synthetic" + this.hashCode())) {
            if (!saved.isEmpty() && saved.peek() != null && saved.peek().event().isPresent()) {
                Optional<Event<Object>> result = Optional.of(new Event<>(
                        "del",
                        Objects.requireNonNull(saved.poll()).event().get().data(),
                        input.timestamp())
                );
                result.ifPresent(Main::logEventTriggerd);
                return new Response(result, List.of());
            }
        }
        return Response.empty();
    }
}
