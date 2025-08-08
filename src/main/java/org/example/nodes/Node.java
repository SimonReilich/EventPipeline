package org.example.nodes;

import org.example.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Node {

    public final long id;

    public Node(long id) {
        this.id = id;
    }

    // dependencies of the node + synthetic signals
    public abstract Set<String> accepts();

    // dependencies of the node
    public abstract Set<String> requires();

    public boolean acceptsAny(Set<String> input) {
        for (var type : input) {
            if (accepts().contains(type)) {
                return true;
            }
        }
        return false;
    }

    protected abstract List<Node> children();

    protected abstract List<Timer> supply(Event<Object> input);

    protected abstract Response trigger(long timestamp);

    public Response give(Event<Object> input) {
        var filtered = input.filter(accepts());
        if (!filtered.getDataSet().isEmpty()) {
            var resp = new Response(Optional.empty(), supply(filtered));
            return trigger(input.timestamp()).merge(resp);
        } else {
            return Response.empty();
        }
    }

    public record Timer(Long timestamp, int target) {
    }

    public record Response(Optional<Event<Object>> event, List<Timer> timers) {
        public static Response empty() {
            return new Response(Optional.empty(), new ArrayList<>());
        }

        public Response merge(Response other) {
            if (this.event.isPresent()) {
                if (other.event.isPresent()) {
                    assert this.event.get().timestamp() == other.event.get().timestamp();
                    var eventMap = this.event.get().data();
                    eventMap.putAll(other.event.get().data());
                    var timerList = this.timers;
                    timerList.addAll(other.timers);
                    return new Response(
                            Optional.of(new Event<>(
                                    eventMap,
                                    this.event.get().timestamp()
                            )),
                            timerList
                    );
                } else {
                    ArrayList<Timer> newTimers = new ArrayList<>();
                    newTimers.addAll(this.timers);
                    newTimers.addAll(other.timers);
                    return new Response(this.event, newTimers);
                }
            } else if (other.event.isPresent()) {
                ArrayList<Timer> newTimers = new ArrayList<>();
                newTimers.addAll(this.timers);
                newTimers.addAll(other.timers);
                return new Response(other.event, newTimers);
            } else {
                ArrayList<Timer> newTimers = new ArrayList<>();
                newTimers.addAll(this.timers);
                newTimers.addAll(other.timers);
                return new Response(Optional.empty(), newTimers);
            }
        }

        public SaveResponse save() {
            assert this.event.isPresent();
            return new SaveResponse(
                    this.event().get(),
                    this.timers()
            );
        }
    }

    public record SaveResponse(Event<Object> event, List<Timer> timers) {
    }
}
