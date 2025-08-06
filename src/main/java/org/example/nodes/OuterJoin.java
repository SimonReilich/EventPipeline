package org.example.nodes;

import org.example.events.Event;

import java.util.*;

public class OuterJoin extends Node {

    protected final String driving;
    protected final Set<String> types;
    protected final Map<String, Event<?>> state;

    public OuterJoin(String driving, String... types) {
        this.driving = driving;
        this.types = new HashSet<>();
        this.types.add(driving);
        this.types.addAll(Arrays.asList(types));
        this.state = new HashMap<>();
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
        return "OuterJoin(" + driving + ", "
                + String.join(", ", types.stream().filter(s -> !s.equals(driving)).toList())
                + ")[" + this.hashCode() + "]";
    }

    public Optional<Event<?>> giveSingle(Event<?> e) {
        if (types.contains(e.getName())) {
            state.put(e.getName(), e);
            int size = state.size();
            var output = state.values().toArray(Event[]::new);
            if (e.getName().equals(driving)) {
                state.clear();
            }
            if (size == types.size() && output.length > 0) {
                return Optional.of(new Event<>(
                        getOutput(),
                        Arrays.stream(output).map(Event::getData).toArray(),
                        e.getTimestamp()
                ));
            }
        }
        return Optional.empty();
    }
}