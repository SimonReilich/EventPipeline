package org.example.events;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractEvent {

    private long timestamp;

    public AbstractEvent(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public abstract Set<String> getAllTypes();

    public abstract Object getValue(String type);

    public abstract List<Event<?>> toList();

    public abstract Stream<Event<?>> stream();
}