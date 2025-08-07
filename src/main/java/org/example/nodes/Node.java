package org.example.nodes;

import org.example.events.Event;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Node {

    // dependencies of the node + synthetic signals
    public abstract Set<String> accepts();

    protected abstract List<Node> children();

    public abstract String getOutputSignalName();

    protected abstract void supply(Event<?> input);

    protected abstract Optional<Event<?>> trigger(Event<?> input);

    public Optional<Event<Map<String, ?>>> giveGroup(Event<Map<String, ?>> input) {
        var results = input.getData().entrySet().stream()
                .map(e -> give(new Event<>(
                        e.getKey(),
                        e.getValue(),
                        input.getTimestamp()
                )))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Event::getName, Event::getData));
        if (results.isEmpty()) return Optional.empty();

        return Optional.of(new Event<>(
                "Group",
                results,
                input.getTimestamp()
        ));
    }

    public Optional<Event<?>> give(Event<?> input) {
        if (accepts().contains(input.getName())) {
            supply(input);
            return trigger(input);
        } else {
            return Optional.empty();
        }
    }

}
