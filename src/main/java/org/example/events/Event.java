package org.example.events;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Event<T> implements Comparable<Event<T>> {

    private final String type;
    private final T data;
    private final long timestamp;

    public Event(String type, T data, long timestamp) {
        this.timestamp = timestamp;
        this.type = type;
        this.data = data;
    }

    public static String dataToString(Object data) {
        return switch (data) {
            case Object[] objects ->
                    "[" + String.join(", ", Arrays.stream(objects).map(Event::dataToString).toList()) + "]";
            case Map.Entry<?, ?> entry ->
                    "\n        (" + dataToString(entry.getKey()) + " => " + dataToString(entry.getValue()) + ")";
            case int[] ints -> "[" + String.join(", ", Arrays.stream(ints).mapToObj(Integer::toString).toList()) + "]";
            case long[] longs -> "[" + String.join(", ", Arrays.stream(longs).mapToObj(Long::toString).toList()) + "]";
            case double[] doubles ->
                    "[" + String.join(", ", Arrays.stream(doubles).mapToObj(Double::toString).toList()) + "]";
            case null -> "-";
            default -> data.toString();
        };
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return this.type;
    }

    public T getData() {
        return this.data;
    }

    public Set<String> getAllTypes() {
        return Set.of(this.type);
    }

    public T getValue(String type) {
        return type.equals(this.type) ? data : null;
    }

    public int compareTo(Event<T> o) {
        return Long.compare(this.getTimestamp(), o.getTimestamp());
    }

    public String toString() {
        return getName() + "; " + dataToString(data) + "; " + getTimestamp();
    }
}