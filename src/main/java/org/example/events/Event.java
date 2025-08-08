package org.example.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Event<T> implements Comparable<Event<T>> {

    private final Map<String, T> data;
    private final long timestamp;

    public Event(String type, T data, long timestamp) {
        this.timestamp = timestamp;
        this.data = new HashMap<>();
        this.data.put(type, data);
    }

    public Event(Map<String, T> data, long timestamp) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public static String dataToString(Object data) {
        return switch (data) {
            case int[] ints -> "[" + String.join(", ", Arrays.stream(ints).mapToObj(Integer::toString).toList()) + "]";
            case long[] longs -> "[" + String.join(", ", Arrays.stream(longs).mapToObj(Long::toString).toList()) + "]";
            case double[] doubles ->
                    "[" + String.join(", ", Arrays.stream(doubles).mapToObj(Double::toString).toList()) + "]";
            case Object[] objects ->
                    "[" + String.join(", ", Arrays.stream(objects).map(Event::dataToString).toList()) + "]";
            case Map.Entry<?, ?> entry ->
                    "\n        (" + dataToString(entry.getKey()) + " => " + dataToString(entry.getValue()) + ")";
            case null -> "-";
            default -> data.toString();
        };
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Set<String> getTypes() {
        return this.data.keySet();
    }

    public Set<Map.Entry<String, T>> getDataSet() {
        return this.data.entrySet();
    }

    public Map<String, T> getData() {
        return this.data;
    }

    public T getValue(String type) {
        return this.data.get(type);
    }

    public int compareTo(Event<T> o) {
        return Long.compare(this.getTimestamp(), o.getTimestamp());
    }

    public String toString() {
        return getTypes() + "; " + dataToString(data) + "; " + getTimestamp();
    }

    public Event<T> filter (Set<String> types) {
        return new Event<>(
                this.data.entrySet().stream()
                        .filter(e -> types.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                this.timestamp
        );
    }
}