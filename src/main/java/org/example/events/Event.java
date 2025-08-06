package org.example.events;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Event<T> extends AbstractEvent implements Comparable<Event<T>> {

    private final String type;
    private final T data;

    public Event(String type, T data, long timestamp) {
        super(timestamp);
        this.type = type;
        this.data = data;
    }

    private static String dataToString(Object data) {
        return switch (data) {
            case Object[] objects ->
                    "[" + String.join(", ", Arrays.stream(objects).map(Event::dataToString).toList()) + "]";
            case int[] ints ->
                    "[" + String.join(", ", Arrays.stream(ints).mapToObj(Integer::toString).toList()) + "]";
            case long[] longs ->
                    "[" + String.join(", ", Arrays.stream(longs).mapToObj(Long::toString).toList()) + "]";
            case double[] doubles ->
                    "[" + String.join(", ", Arrays.stream(doubles).mapToObj(Double::toString).toList()) + "]";
            case null -> "-";
            default -> data.toString();
        };
    }

    public String getName() {
        return this.type;
    }

    public T getData() {
        return this.data;
    }

    @Override
    public Set<String> getAllTypes() {
        return Set.of(this.type);
    }

    @Override
    public T getValue(String type) {
        return type.equals(this.type) ? data : null;
    }

    @Override
    public List<Event<?>> toList() {
        return List.of(this);
    }

    @Override
    public Stream<Event<?>> stream() {
        return Stream.of(this);
    }

    @Override
    public int compareTo(Event<T> o) {
        return Long.compare(this.getTimestamp(), o.getTimestamp());
    }

    @Override
    public String toString() {
        return getName() + "; " + dataToString(data) + "; " + getTimestamp();
    }

    public String toStringPretty() {
        if (type.contains("[") && type.endsWith("]")) {
            return type.substring(0, type.lastIndexOf('[')) + "; " + dataToString(data) + "; " + getTimestamp();
        } else {
            return toString();
        }
    }
}