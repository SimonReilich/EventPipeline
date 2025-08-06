package org.example;

import org.example.events.AbstractEvent;
import org.example.events.Event;
import org.example.events.EventGroup;
import org.example.nodes.Node;
import org.example.nodes.Previous;
import org.example.nodes.Window;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class Main {

    private final static long INPUT_DELAY = 500;
    private final static long MAX_IDLE = 30_000;

    private static EventQueue queue;
    private static NodeList nodes;
    private static ScheduledExecutorService executor;
    private static int count;

    public static void main(String[] args) {

        queue = new EventQueue();
        nodes = new NodeList();

        var window = new Window("A", 1000, 1000, Window.TIME, Window.TIME);
        var prev = new Previous(window.getOutput());

        nodes.add(prev);
        nodes.add(window);

        count = 0;
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> onEvent(new Event<>(
                "System",
                null,
                System.currentTimeMillis())
        ), 0, INPUT_DELAY, TimeUnit.MILLISECONDS);
        System.out.println();

        try {
            sleep(2000);
            long ts = System.currentTimeMillis();
            onEvent(new Event<>("A", 3.1415, ts));
            onEvent(new Event<>("A", "hello", ts + 500));
            onEvent(new Event<>("A", true, ts + 1000));
            onEvent(new Event<>("A", false, ts + 1500));
            onEvent(new Event<>("A", new int[]{1, 2, 3}, ts + 5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void onEvent(Event<?> e) {
        if (!Objects.equals(e.getName(), "System")) {
            executor.shutdown();
            queue.add(e);
        } else {
            count++;
            if (count >= MAX_IDLE / INPUT_DELAY
                    && queue.stream()
                    .allMatch(event -> event.getName().equals("System"))
            ) {
                executor.shutdown();
                System.err.println("[ERROR]: System has been terminated, no events arrived after waiting "
                        + (MAX_IDLE / 1_000f) + "s ...");
                return;
            }
        }
        while (queue.peek() != null && queue.peek().getTimestamp() + INPUT_DELAY < System.currentTimeMillis()) {
            assert queue.peek() != null;
            logEvent(queue.peek());
            runPipeline(new EventGroup(queue.poll()));
            count = 0;
        }
        if (executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(() -> onEvent(new Event<>(
                    "System",
                    null,
                    System.currentTimeMillis())
            ), INPUT_DELAY, INPUT_DELAY, TimeUnit.MILLISECONDS);
        }
    }

    private static void runPipeline(EventGroup events) {
        for (Node node : nodes) {
            Optional<? extends AbstractEvent> res = node.give(events);
            if (res.isPresent()) {
                for (Event<?> event : res.get().toList()) {
                    logEvent(event);
                    events.add(event);
                }
            }
        }
        System.out.println();
    }

    protected static void notifyExecutor() {
        executor.shutdownNow();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> onEvent(new Event<>(
                "System",
                null,
                System.currentTimeMillis())
        ), 0, INPUT_DELAY, TimeUnit.MILLISECONDS);
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
}