package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;

public class Delay extends Node {

    private final long delay;
    private final Node node;
    private final Queue<Event<Object>> saved;

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
    protected void supply(Event<Object> input) {
        Main.logEventSupplied(input, "Delay");
        var res = node.give(input);
        if (res.isPresent()) {
            saved.add(res.get());
            Main.addEvent(new Event<>(
                    "Synthetic" + this.hashCode(),
                    null,
                    input.timestamp() + this.delay
            ));
        }
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        if (input.getTypes().contains("Synthetic" + this.hashCode())) {
            if (!saved.isEmpty() && saved.peek() != null) {
                Optional<Event<Object>> result = Optional.of(new Event<>(
                        Objects.requireNonNull(saved.poll()).data(),
                        input.timestamp())
                );
                result.ifPresent(r -> Main.logEventTriggerd(r, "Delay"));
                return result;
            }
        }
        return Optional.empty();
    }

}
