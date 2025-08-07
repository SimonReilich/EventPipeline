//package org.example.events;
//
//import java.awt.*;
//import java.util.*;
//import java.util.List;
//import java.util.stream.Stream;
//
//public class EventGroup extends AbstractEvent {
//
//    public final Map<String, Object> events;
//
//    public EventGroup(Event<?>... events) {
//        super(events[0].getTimestamp());
//        this.events = new HashMap<>();
//        for (Event<?> event : events) {
//            this.events.put(event.getName(), event.getData());
//            if (event.getTimestamp() > getTimestamp()) {
//                setTimestamp(event.getTimestamp());
//            }
//        }
//    }
//
//    public EventGroup(EventGroup eventGroup, long timestamp) {
//        super(timestamp);
//        this.events = new HashMap<>();
//        this.events.putAll(eventGroup.events);
//    }
//
//    @Override
//    public Set<String> getAllTypes() {
//        return events.keySet();
//    }
//
//    @Override
//    public Object getValue(String type) {
//        return events.get(type);
//    }
//
//    @Override
//    public List<Map.Entry<String, ?>> toList() {
//        return new ArrayList<>(events.entrySet());
//    }
//
//    public Stream<Event<?>> eventStream() {
//        return events.entrySet().stream()
//                .map(e -> new Event<>(e.getKey(), e.getValue(), this.getTimestamp()));
//    }
//
//    public void merge(AbstractEvent other) {
//        if (other instanceof EventGroup) {
//            this.events.putAll(((EventGroup) other).events);
//        } else if (other instanceof Event) {
//            this.events.put(((Event<?>) other).getName(), ((Event<?>) other).getData());
//        }
//    }
//}