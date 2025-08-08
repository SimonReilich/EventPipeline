package org.example;

import org.example.events.Event;
import org.example.nodes.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

public class EventPipeline {

    private final static long INPUT_DELAY = 500;
    private final PriorityQueue<Event<Object>> queue;
    private final ArrayList<Node> nodes;
    private final Consumer<Event<Object>> consumer;
    private Thread thread;

    public EventPipeline(Consumer<Event<Object>> consumer) {
        queue = new PriorityQueue<>();
        nodes = new ArrayList<>();
        this.consumer = consumer;
    }

    private void addEvent(Event<Object> event) {
        queue.add(event);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    private void initThread() {
        thread = new Thread(() -> {
            try {
                assert queue.peek() != null;
                sleep((queue.peek().timestamp() + INPUT_DELAY) - System.currentTimeMillis());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            onEvent(new Event<>(
                    "System",
                    null,
                    System.currentTimeMillis())
            );
        });
        thread.start();
    }

    public void onEvent(Event<Object> e) {
        if (Thread.currentThread().getName().equals("main")) {
            if (thread != null && thread.isAlive()) {
                try {
                    thread.join();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            queue.add(e);
        }
        synchronized (queue) {
            while (queue.peek() != null && queue.peek().timestamp() + INPUT_DELAY <= System.currentTimeMillis()) {
                assert queue.peek() != null;
                runPipeline(queue.poll());
            }
            if (!queue.isEmpty()) {
                initThread();
            }
        }
    }

    private void runPipeline(Event<Object> event) {
        Map<String, Object> dataMap = new HashMap<>();
        Event<Object> events = new Event<>(
                dataMap,
                event.timestamp()
        );
        dataMap.putAll(event.data());
        for (Node node : nodes) {
            Node.Response res = node.give(events);
            res.event().ifPresent(e -> dataMap.putAll(e.data()));
            res.timers().forEach(t -> addEvent(new Event<>(
                    "Synthetic" + t.target(),
                    Map.of(),
                    t.timestamp()
            )));
        }
        this.consumer.accept(events);
    }
}