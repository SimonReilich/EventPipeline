package org.example;

import org.example.events.Event;
import org.example.nodes.*;

import java.util.*;

import static java.lang.Thread.sleep;

public class Main {

    private final static long INPUT_DELAY = 500;

    private static PriorityQueue<Event<?>> queue;
    private static ArrayList<Node> nodes;
    private static Thread thread;

    public static void main(String[] args) {

        queue = new PriorityQueue<>();
        nodes = new ArrayList<>();

        nodes.add(new Group(
                1000,
                new CombineLatest(
                        new RawInput("A"),
                        new Previous(
                                new RawInput("B")
                        )
                ),
                new RawInput("C")
        ));

        System.out.println();

        try {
            sleep(2000);
            long ts = System.currentTimeMillis() + 1000;
            onEvent(new Event<>("A", 3.1415, ts));
            onEvent(new Event<>("C", 1.5, ts));
            onEvent(new Event<>("C", 2.5, ts + 200));
            onEvent(new Event<>("B", "hello", ts + 500));
            onEvent(new Event<>("A", true, ts + 1000));
            onEvent(new Event<>("B", false, ts + 1500));
            onEvent(new Event<>("C", 3.5, ts + 4200));
            onEvent(new Event<>("A", new int[]{1, 2, 3}, ts + 5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initThread() {
        thread = new Thread(() -> {
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
        thread.start();
    }

    private static void onEvent(Event<?> e) {
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
            while (queue.peek() != null && queue.peek().getTimestamp() + INPUT_DELAY <= System.currentTimeMillis()) {
                assert queue.peek() != null;
                runPipeline(queue.poll());
            }
            if (!queue.isEmpty()) {
                initThread();
            }
        }
    }

    private static void runPipeline(Event<?> event) {
        Map<String, Object> dataMap = new HashMap<>();
        Event<Map<String, ?>> events = new Event<>(
                "Group",
                dataMap,
                event.getTimestamp()
        );
        dataMap.put(event.getName(), event.getData());
        for (Node node : nodes) {
            Optional<Event<Map<String, ?>>> res = node.giveGroup(events);
            res.ifPresent(e -> dataMap.putAll(e.getData()));
        }
        System.out.println();
    }

    public static void addEvent(Event<?> event) {
        queue.add(event);
    }

    public static void addNode(Node node) {
        nodes.add(node);
    }

    public static void logEvent(Event<?> event) {
        long ts = System.currentTimeMillis();
        System.out.println("[EVENT]: " + event.toString()
                + " (delay of " + (ts - event.getTimestamp()) + "ms)");
    }
}