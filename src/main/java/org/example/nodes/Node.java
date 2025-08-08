package org.example.nodes;

import org.example.events.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Node {

    // dependencies of the node + synthetic signals
    public abstract Set<String> accepts();

    // dependencies of the node
    public abstract Set<String> requires();

    public boolean acceptsAny(Set<String> input) {
        for (var type: input) {
            if (accepts().contains(type)) {
                return true;
            }
        }
        return false;
    }

    protected abstract List<Node> children();

    protected abstract void supply(Event<Object> input);

    protected abstract Optional<Event<Object>> trigger(Event<Object> input);

    public Optional<Event<Object>> give(Event<Object> input) {
        var filtered = input.filter(accepts());
        if (!filtered.getDataSet().isEmpty()) {
            supply(filtered);
            return trigger(filtered);
        } else {
            return Optional.empty();
        }
    }

}
