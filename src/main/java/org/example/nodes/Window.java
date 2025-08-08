package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;
import java.util.stream.Collectors;

public class Window extends Node {

    public static final Character ITEM = 'I';
    public static final Character TIME = 'T';
    public static final Character GROUP = 'G';
    private final Node node;
    private final List<Map.Entry<String, Map.Entry<Object, Long>>> queue;
    private final Character rateMode;
    private final long rate;
    private final Character sizeMode;
    private final long size;
    private long counter;

    public Window(Character rateMode, long rate, Character sizeMode, long size, Node node) {
        super();

        assert rateMode == ITEM || rateMode == TIME || rateMode == GROUP;
        assert sizeMode == ITEM || sizeMode == TIME;

        this.rateMode = rateMode;
        this.rate = rate;
        this.sizeMode = sizeMode;
        this.size = size;
        this.node = node;
        this.queue = new LinkedList<>();
    }

    @Override
    public Set<String> accepts() {
        Set<String> accepts = new HashSet<>();
        accepts.add("Synthetic" + this.hashCode());
        accepts.add("SyntheticG" + this.hashCode());
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
        Main.logEventSupplied(input, "Window");
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
            result.get().getDataSet()
                    .forEach(e -> queue.removeIf(entry -> Objects.equals(entry.getKey(), e.getKey())));
            queue.addAll(result.get().getDataSet().stream()
                    .map(e -> Map.entry(e.getKey(), Map.entry(e.getValue(), input.getTimestamp())))
                    .collect(Collectors.toSet()));
        }
    }

    @Override
    protected Optional<Event<Object>> trigger(Event<Object> input) {
        if (this.acceptsAny(input.getTypes())) {
            if (sizeMode == ITEM) {
                while (queue.size() > size) {
                    queue.removeFirst();
                }
            } else if (sizeMode == TIME) {
                while (queue.getFirst() != null && queue.getFirst().getValue().getValue() < input.getTimestamp() - size) {
                    queue.removeFirst();
                }
            }

            if (rateMode == ITEM) {
                counter++;
                if (counter >= rate) {
                    counter = 0;
                    Optional<Event<Object>> res = Optional.of(new Event<>(
                            queue.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getKey())),
                            input.getTimestamp()
                    ));
                    queue.clear();
                    Main.logEventTriggerd(res.get(), "Window");
                    return res;
                }
            } else if (rateMode == TIME && input.getTypes().contains("Synthetic" + this.hashCode())) {
                Optional<Event<Object>> res = Optional.of(new Event<>(
                        queue.stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getKey())),
                        input.getTimestamp()
                ));
                queue.clear();
                Main.logEventTriggerd(res.get(), "Window");
                return res;
            } else if (rateMode == GROUP && input.getTypes().contains("SyntheticG" + this.hashCode())) {
                Optional<Event<Object>> res = Optional.of(new Event<>(
                        queue.stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getKey())),
                        input.getTimestamp()
                ));
                queue.clear();
                Main.logEventTriggerd(res.get(), "Window");
                return res;
            }
        }

        return Optional.empty();
    }

}
