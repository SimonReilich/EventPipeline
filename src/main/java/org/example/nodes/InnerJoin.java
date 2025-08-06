package org.example.nodes;

import org.example.events.Event;

import java.util.*;

public class InnerJoin extends Node {

    protected final String driving;
    protected final Set<String> types;
    protected final Map<String, Event<?>> state = new HashMap<>();

    public InnerJoin(String driving, String... types) {
        this.driving = driving;
        this.types = new HashSet<>();
        this.types.add(driving);
        this.types.addAll(Arrays.asList(types));
    }

    @Override
    public Set<String> accepts() {
        return types;
    }

    @Override
    public Set<String> requires() {
        return accepts();
    }

    @Override
    public String getOutput() {
        return "InnerJoin(" + driving + ", "
                + String.join(", ", types.stream().filter(s -> !s.equals(driving)).toList())
                + ")[" + this.hashCode() + "]";
    }

    public Optional<Event<?>> giveSingle(Event<?> e) {
        if (types.contains(e.getName())) {
            state.put(e.getName(), e);
            var output = state.values().toArray(Event[]::new);
            if (e.getName().equals(driving)) {
                state.clear();
            }
            return Optional.of(new Event<>(
                    getOutput(),
                    Arrays.stream(output).map(Event::getData).toArray(),
                    e.getTimestamp()
            ));
        }
        return Optional.empty();
    }
}