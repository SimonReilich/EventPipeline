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
            case HashMap<?, ?> map -> mapToString(map, depth);
            case int[] ints -> "[" + String.join(", ", Arrays.stream(ints).mapToObj(Integer::toString).toList()) + "]";
            case long[] longs -> "[" + String.join(", ", Arrays.stream(longs).mapToObj(Long::toString).toList()) + "]";
            case double[] doubles ->
                    "[" + String.join(", ", Arrays.stream(doubles).mapToObj(Double::toString).toList()) + "]";
            case Object[] objects ->
                    "[" + String.join(", ", Arrays.stream(objects).map(e -> Event.dataToString(e, depth)).toList()) + "]";
            case Map.Entry<?, ?> entry -> entryToString(entry, depth);
            case null -> "-";
            default -> data.toString();
        };
    }

    private static String mapToString(Map<?, ?> map, int depth) {
        if (isSingleLineMap(map)) {
            var entry = map.entrySet().iterator().next();
            return entryToString(entry, depth).replace("\n", "").strip();
        } else {
            return "(" + map.entrySet().stream()
                    .map(e -> dataToString(e, depth + 1))
                    .collect(Collectors.joining()) + "\n" + " ".repeat(4 * depth) + ")";
        }
    }

    private static String entryToString(Map.Entry<?, ?> entry, int depth) {
        if (entry.getValue() instanceof Map<?, ?> && isSingleLineMap((Map<?, ?>) entry.getValue())) {
            var valueStr = dataToString(entry.getValue(), depth);
            if (valueStr.split("\r\n|\r|\n").length > 0) {
                return "\n" + " ".repeat(4 * depth) + dataToString(entry.getKey(), 0) + "." + valueStr;
            } else {
                return dataToString(entry.getKey(), 0) + "." + dataToString(entry.getValue(), 0);
            }
        } else {
            var valueStr = dataToString(entry.getValue(), depth);
            if (valueStr.split("\r\n|\r|\n").length > 0) {
                return "\n" + " ".repeat(4 * depth) + dataToString(entry.getKey(), 0) + " => " + valueStr;
            } else {
                return dataToString(entry.getKey(), 0) + " => " + dataToString(entry.getValue(), 0);
            }
        }
    }

    private static boolean isSingleLineMap(Map<?, ?> map) {
        if (map.size() == 1) {
            var entry = map.entrySet().iterator().next();
            if (entry.getValue() instanceof Map<?, ?>) {
                return isSingleLineMap((Map<?, ?>) entry.getValue());
            }
            return true;
        } else {
            return false;
        }
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

    @Override
    public String toString() {
        return dataToString(data, 0) + "; " + timestamp();
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