package org.example.nodes;

import org.example.events.Event;

import java.util.Optional;
import java.util.Set;

public class Previous extends Node {

    private final String type;
    protected Event<?> state;
    protected Event<?> lastElem;

    public Previous(String type) {
        this.type = type;
    }

    @Override
    public Set<String> accepts() {
        return Set.of(type);
    }

    @Override
    public Set<String> requires() {
        return accepts();
    }

    @Override
    public Optional<Event<?>> giveSingle(Event<?> e) {
        if (e.getName().equals(this.type)) {

            Optional<Event<?>> result = (state == null) ? Optional.empty() : Optional.of(new Event<>(
                    getOutput(),
                    state.getValue(type),
                    e.getTimestamp()
            ));
            state = e;
            return result;
        }
        return Optional.empty();
    }

    @Override
    public String getOutput() {
        return "Previous(" + type + ")[" + this.hashCode() + "]";
    }

    protected Previous copy() {
        return new Previous(type);
    }
}