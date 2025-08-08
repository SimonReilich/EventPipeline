package org.example;

import org.example.events.Event;
import org.example.nodes.*;

import java.util.*;

import static java.lang.Thread.sleep;

public class Main {

    public static Main instance;

    private final static long INPUT_DELAY = 500;

    private final PriorityQueue<Event<Object>> queue;
    private final ArrayList<Node> nodes;
    private Thread thread;

    private static boolean log = false;

    public static void main(String[] args) {

        instance = new Main();

        System.out.println();

        try {
            sleep(2000);
            long ts = System.currentTimeMillis() + 1000;
            instance.onEvent(new Event<>("B", "hi", ts));
            instance.onEvent(new Event<>("A", false, ts));
            instance.onEvent(new Event<>("C", 1.5, ts));
            instance.onEvent(new Event<>("C", 2.5, ts + 200));
            instance.onEvent(new Event<>("B", "String", ts + 400));
            instance.onEvent(new Event<>("B", "hello", ts + 500));
            instance.onEvent(new Event<>("A", true, ts + 1000));
            instance.onEvent(new Event<>("B", "test", ts + 1500));
            instance.onEvent(new Event<>("C", 3.5, ts + 4200));
            instance.onEvent(new Event<>("A", new int[]{1, 2, 3}, ts + 5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Main() {

        queue = new PriorityQueue<>();
        nodes = new ArrayList<>();

        addNode(
                new CombineLatest(
                                new OuterJoin(
                                        new RawInput("A"),
                                        new RawInput("C")
                                ),
                        new Previous(
                                        new RawInput("B")
                        ),
                        new RawInput("B")
                )
        );
    }

    public void initThread() {
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

    private void onEvent(Event<Object> e) {
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
        if (log) {
            System.out.println();
            log = false;
        }
    }

    public static void addEvent(Event<Object> event) {
        instance.addEventObj(event);
    }

    private void addEventObj(Event<Object> event) {
        queue.add(event);
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public static void logEventTriggerd(Event<?> event) {
        long ts = System.currentTimeMillis();
        System.out.println("[TRIGGERD]: " + event.toString()
                + " (delay of " + (ts - event.timestamp()) + "ms)");
        log = true;
    }

    public static void logEventSupplied(Event<?> event) {
        long ts = System.currentTimeMillis();
        System.out.println("[SUPPLIED]: " + event.toString()
                + " (delay of " + (ts - event.timestamp()) + "ms)");
        log = true;
    }
}