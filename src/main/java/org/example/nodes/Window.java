package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;

public class Window extends Node {

    public static final Character ITEM = 'I';
    public static final Character TIME = 'T';

    private Character rateMode;
    private long rate;
    private Character sizeMode;
    private long size;
    private final Node node;
    private final PriorityQueue<Event<?>> queue;

    private long counter;

    public Window(Character rateMode, long rate, Character sizeMode, long size, Node node) {
        super();

        assert rateMode == ITEM || rateMode == TIME;
        assert sizeMode == ITEM || sizeMode == TIME;

        this.rateMode = rateMode;
        this.rate = rate;
        this.sizeMode = sizeMode;
        this.size = size;
        this.node = node;
        this.queue = new PriorityQueue<>();
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
        return "Window(" + node.getOutputSignalName() + ")[" + this.hashCode() + "]";
    }

    @Override
    protected void supply(Event<?> input) {
        System.out.println("supplied to queue");
        var result = node.give(input);
        if (result.isPresent()) {
            if (queue.isEmpty()) {
                if (rateMode == TIME) {
                    Main.addEvent(new Event<>(
                            "Synthetic" + this.hashCode(),
                            null,
                            input.getTimestamp() + this.rate
                    ));
                }
            }
            queue.add(result.get());
        }
    }

    @Override
    protected Optional<Event<?>> trigger(Event<?> input) {
        System.out.println("triggered queue");
        if (this.accepts().contains(input.getName())) {
            if (sizeMode == ITEM) {
                while (queue.size() > size) {
                    queue.poll();
                }
            } else if (sizeMode == TIME) {
                while (queue.peek() != null && queue.peek().getTimestamp() < input.getTimestamp() - size) {
                    queue.poll();
                }
            }

            if (rateMode == ITEM) {
                counter++;
                if (counter >= rate) {
                    counter = 0;
                    Optional<Event<?>> res = Optional.of(new Event<>(
                            getOutputSignalName(),
                            queue.stream().map(Event::getData).toArray(),
                            input.getTimestamp()
                    ));
                    queue.clear();
                    Main.logEvent(res.get());
                    return res;
                }
            } else if (rateMode == TIME && input.getName().equals("Synthetic" + this.hashCode())) {
                Optional<Event<?>> res = Optional.of(new Event<>(
                        getOutputSignalName(),
                        queue.toArray(),
                        input.getTimestamp()
                ));
                queue.clear();
                Main.logEvent(res.get());
                return res;
            }
        }

        return Optional.empty();
    }
}
