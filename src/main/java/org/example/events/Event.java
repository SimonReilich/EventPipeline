package org.example.events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Event<T>(Map<String, T> data, long timestamp) implements Comparable<Event<T>> {

    public Event(String type, T data, long timestamp) {
        this(new HashMap<>(), timestamp);
        this.data.put(type, data);
    }

    public static String dataToString(Object data, int depth) {
        return switch (data) {
            case HashMap<?, ?> map ->
                    dataToString(map.entrySet().toArray(), depth);
            case int[] ints ->
                    new String(new char[depth * 4]).replace("\0", " ") + "[" + String.join(", ", Arrays.stream(ints).mapToObj(Integer::toString).toList()) + "]";
            case long[] longs ->
                    new String(new char[depth * 4]).replace("\0", " ") + "[" + String.join(", ", Arrays.stream(longs).mapToObj(Long::toString).toList()) + "]";
            case double[] doubles ->
                    new String(new char[depth * 4]).replace("\0", " ") + "[" + String.join(", ", Arrays.stream(doubles).mapToObj(Double::toString).toList()) + "]";
            case Object[] objects ->
                    new String(new char[depth * 4]).replace("\0", " ") + "[" + String.join(", ", Arrays.stream(objects).map(e -> Event.dataToString(e, depth + 1)).toList()) + "]";
            case Map.Entry<?, ?> entry ->
                    "\n" + new String(new char[depth * 4]).replace("\0", " ") + dataToString(entry.getKey(), 0) + " => " + dataToString(entry.getValue(), 0);
            case null -> "-";
            default -> data.toString();
        };
    }

    public Set<String> getTypes() {
        return this.data.keySet();
    }

    public Set<Map.Entry<String, T>> getDataSet() {
        return this.data.entrySet();
    }

    public T getValue(String type) {
        return this.data.get(type);
    }

    public int compareTo(Event<T> o) {
        return Long.compare(this.timestamp(), o.timestamp());
    }

    public String toString() {
        return getTypes() + "; " + dataToString(data, 0) + "; " + timestamp();
    }

    public Event<T> filter(Set<String> types) {
        return new Event<>(
                this.data.entrySet().stream()
                        .filter(e -> types.contains(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                this.timestamp
        );
    }
}