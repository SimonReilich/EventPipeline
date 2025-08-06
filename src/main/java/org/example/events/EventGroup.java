package org.example.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventGroup extends AbstractEvent {

    public final List<AbstractEvent> events;

    public EventGroup(AbstractEvent... events) {
        super(events[0].getTimestamp());
        for (AbstractEvent event : events) {
            if (event.getTimestamp() > getTimestamp()) {
                setTimestamp(event.getTimestamp());
            }
        }
        for (AbstractEvent event : events) {
            event.setTimestamp(getTimestamp());
        }

        this.events = new ArrayList<>(Arrays.asList(events));
    }

    @Override
    public void setTimestamp(long timestamp) {
        super.setTimestamp(timestamp);
        for (AbstractEvent event : events) {
            event.setTimestamp(timestamp);
        }
    }

    @Override
    public Set<String> getAllTypes() {
        return events.stream()
                .flatMap(set -> set.getAllTypes().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Object getValue(String type) {
        return events.stream()
                .map(e -> e.getValue(type))
                .filter(s -> !(s == null))
                .findFirst().get();
    }

    @Override
    public List<Event<?>> toList() {
        var list = new ArrayList<Event<?>>();
        for (AbstractEvent event : events) {
            list.addAll(event.toList());
        }
        return list;
    }

    @Override
    public Stream<Event<?>> stream() {
        return events.stream().flatMap(AbstractEvent::stream);
    }

    public void add(Event<?> e) {
        assert this.getTimestamp() == e.getTimestamp();
        this.events.addFirst(e);
        if (e.getTimestamp() > getTimestamp()) {
            setTimestamp(e.getTimestamp());
        }
    }
}