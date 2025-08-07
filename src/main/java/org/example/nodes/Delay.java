package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;

public class Delay extends Node {

    private final long delay;
    private final Node node;
    private final Queue<Event<?>> saved;

    public Delay(long delay, Node node) {
        super();
        this.delay = delay;
        this.node = node;
        this.saved = new LinkedList<>();
    }

    @Override
    protected Set<String> accepts() {
        Set<String> accepts = new HashSet<>();
        accepts.add("Synthetic" + this.hashCode());
        accepts.addAll(node.accepts());
        return accepts;
    }

    @Override
    protected List<Node> children() {
        return List.of(node);
    }

    @Override
    public String getOutputSignalName() {
        return "Delay(" + node.getOutputSignalName() + ")[" + this.hashCode() + "]";
    }

    @Override
    protected void supply(Event<?> input) {
        var res = node.give(input);
        if (res.isPresent()) {
            saved.add(res.get());
            Main.addEvent(new Event<>(
                    "Synthetic" + this.hashCode(),
                    null,
                    input.getTimestamp() + this.delay
            ));
        }
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {
        if (input.getName().equals("Synthetic" + this.hashCode())) {
            if (!saved.isEmpty()) {
                Optional<Event<?>> result = Optional.of(new Event<>(
                        getOutputSignalName(),
                        saved.poll().getData(),
                        input.getTimestamp())
                );
                result.ifPresent(Main::logEvent);
                return result;
            }
        }
        return Optional.empty();
    }
}
