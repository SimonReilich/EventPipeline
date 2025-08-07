package org.example;

import org.example.events.AbstractEvent;
import org.example.events.Event;
import org.example.events.EventGroup;
import org.example.nodes.Node;
import org.example.nodes.Previous;
import org.example.nodes.Window;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.sleep;

public class Main {

    private final static long INPUT_DELAY = 500;

    private static PriorityQueue<Event<?>> queue;
    private static NodeList nodes;
    private static ExecutorService executor;

    public static void main(String[] args) {

        queue = new PriorityQueue<>();
        nodes = new NodeList();

        var window = new Window("A", 1000, 1000, Window.TIME, Window.TIME);
        var prev = new Previous(window.getOutput());

        nodes.add(prev);
        nodes.add(window);
        executor = Executors.newSingleThreadExecutor();

        System.out.println();

        try {
            sleep(2000);
            long ts = System.currentTimeMillis() + 1000;
            onEvent(new Event<>("A", 3.1415, ts));
            onEvent(new Event<>("A", "hello", ts + 500));
            onEvent(new Event<>("A", true, ts + 1000));
            onEvent(new Event<>("A", false, ts + 1500));
            onEvent(new Event<>("A", new int[]{1, 2, 3}, ts + 5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static synchronized void onEvent(Event<?> e) {
        if (Thread.currentThread().getName().equals("main")) {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                executor = Executors.newSingleThreadExecutor();
            }
            queue.add(e);
        }
        while (queue.peek() != null && queue.peek().getTimestamp() + INPUT_DELAY <= System.currentTimeMillis()) {
            assert queue.peek() != null;
            logEvent(queue.peek());
            runPipeline(new EventGroup(queue.poll()));
        }
        if (!queue.isEmpty()) {
            executor.submit(() -> {
                try {
                    assert queue.peek() != null;
                    sleep((queue.peek().getTimestamp() + INPUT_DELAY) - System.currentTimeMillis());
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                onEvent(new Event<>(
                        "System",
                        null,
                        System.currentTimeMillis())
                );
            });
        }
    }

    private static void runPipeline(EventGroup events) {
            for (Node node : nodes) {
                Optional<? extends AbstractEvent> res = node.give(events);
                if (res.isPresent()) {
                    for (var event : res.get().toList()) {
                        logEvent(events.getTimestamp(), event.getKey(), event.getValue());
                    }
                    events.merge(res.get());
                }
            }
            System.out.println();
    }

    public static void addEvent(Event<?> event) {
        queue.add(event);
    }

    public static void addNode(Node node) {
        nodes.add(node);
    }

    private static void logEvent(Event<?> event) {
        long ts = System.currentTimeMillis();
        System.out.println("[EVENT]: " + event.toString()
                + " (delay of " + (ts - event.getTimestamp()) + "ms)");
    }

    private static void logEvent(long timestamp, String type, Object data) {
        long ts = System.currentTimeMillis();
        System.out.println("[EVENT]: " + type + "; " + Event.dataToString(data)
                + " (delay of " + (ts - timestamp) + "ms)");
    }
}