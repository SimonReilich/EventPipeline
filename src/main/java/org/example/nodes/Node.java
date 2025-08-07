package org.example.nodes;

import org.example.events.AbstractEvent;
import org.example.events.Event;
import org.example.events.EventGroup;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public abstract class Node {

    protected Function<Event<?>, Optional<Event<?>>> injected;

    public abstract Set<String> accepts();

    public abstract Set<String> requires();

    public final Optional<? extends AbstractEvent> give(AbstractEvent e) {
        if (e instanceof Event<?>) {
            return injected == null ? this.giveSingle((Event<?>) e) : injected.apply((Event<?>) e);
        } else if (e instanceof EventGroup) {
            return this.gives((EventGroup) e);
        }
        return Optional.empty();
    }

    private Optional<EventGroup> gives(EventGroup e) {

        var events = e.eventStream()
                .map(event -> {
                    if (injected == null) {
                        return giveSingle(event);
                    } else {
                        return injected.apply(event);
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(Event<?>[]::new);

        if (events.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(new EventGroup(events));
        }
    }

    protected abstract Optional<Event<?>> giveSingle(Event<?> e);

    public abstract String getOutput();

    protected void inject(Function<Event<?>, Optional<Event<?>>> giveSingle) {
        this.injected = giveSingle;
    }
}