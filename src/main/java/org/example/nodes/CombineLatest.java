package org.example.nodes;

import org.example.events.Event;

import java.util.*;

public class CombineLatest extends Node {

    protected final Map<String, Event<?>> state;
    protected final Set<String> types;

    public CombineLatest(String... types) {
        state = new HashMap<>();
        this.types = new HashSet<>();
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

    public Optional<Event<?>> giveSingle(Event<?> e) {
        if (accepts().contains(e.getName())) {
            state.put(e.getName(), e);
            return Optional.of(new Event<>(
                    getOutput(),
                    types.stream().map(key -> {
                        if (state.containsKey(key)) {
                            return state.get(key).getData();
                        } else {
                            return "_";
                        }
                    }).toArray(),
                    e.getTimestamp()
            ));
        }
        return Optional.empty();
    }

    public String getOutput() {
        return "CombineLatest("
                + String.join(", ", types)
                + ")[" + this.hashCode() + "]";
    }
}