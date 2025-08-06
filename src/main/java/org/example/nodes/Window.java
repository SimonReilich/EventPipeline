package org.example.nodes;

import org.example.Main;
import org.example.events.Event;

import java.util.*;

public class Window extends Node {

    public final static Character TIME = 'T';
    public final static Character ITEM = 'I';

    protected final String type;
    protected final long size;
    protected final long rate;
    protected final Character sizeMode;
    protected final Character rateMode;
    protected final Queue<Event<?>> buffer;
    protected long countdown;
    protected boolean first;
    protected String synthSignalName;

    public Window(String eventType, long size, long rate, Character sizeMode, Character rateMode) {
        this.type = eventType;
        this.size = size;
        this.rate = rate;
        this.countdown = rate;

        assert sizeMode == TIME || sizeMode == ITEM;
        assert rateMode == TIME || rateMode == ITEM;

        this.sizeMode = sizeMode;
        this.rateMode = rateMode;
        this.buffer = new PriorityQueue<>();
        this.first = true;

        this.synthSignalName = "Synth" + this.hashCode();
    }

    @Override
    public Set<String> accepts() {
        if (sizeMode.equals(ITEM) && rateMode.equals(ITEM)) {
            return Set.of(type);
        } else {
            return Set.of(type, synthSignalName);
        }
    }

    @Override
    public Set<String> requires() {
        return Set.of(type);
    }

    @Override
    public String getOutput() {
        return "Window(" + type + ")[" + this.hashCode() + "]";
    }

    public Optional<Event<?>> giveSingle(Event<?> e) {
        if (Objects.equals(e.getName(), type)) {
            buffer.add(e);
            countdown--;
        }

        if (Objects.equals(sizeMode, TIME)) {
            buffer.removeIf(event -> event.getTimestamp() + rate < e.getTimestamp());
        } else if (Objects.equals(sizeMode, ITEM)) {
            while (buffer.size() > size) {
                buffer.remove();
            }
        }

        var output = new Event<?>[0];

        if (Objects.equals(rateMode, TIME) && e.getName().equals(type) && first) {
            first = false;
            Main.addEvent(new Event<>(
                    synthSignalName,
                    null,
                    (((int) (e.getTimestamp() / rate)) * rate) + rate)
            );
        } else if (Objects.equals(rateMode, TIME) && e.getName().equals(synthSignalName)) {
            if (!buffer.isEmpty()) {
                Main.addEvent(new Event<>(synthSignalName, null, e.getTimestamp() + rate));
                output = buffer.toArray(Event<?>[]::new);
            } else {
                first = true;
            }
            buffer.clear();
        } else if (Objects.equals(rateMode, ITEM) && countdown <= 0) {
            countdown = rate;
            output = buffer.toArray(Event<?>[]::new);
            buffer.clear();
        }

        if (output.length > 0) {
            return Optional.of(new Event<>(getOutput(),
                    Arrays.stream(output).map(Event::getData).toArray(),
                    e.getTimestamp()));
        }
        return Optional.empty();
    }

    public Window copy() {
        return new Window(type, size, rate, sizeMode, rateMode);
    }
}