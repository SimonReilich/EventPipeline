package org.example;

import org.example.events.Event;
import org.example.nodes.CombineLatest;
import org.example.nodes.OuterJoin;
import org.example.nodes.Previous;
import org.example.nodes.RawInput;

import static java.lang.Thread.sleep;

public class Main {

    private static EventPipeline instance;

    public static void main(String[] args) {

        instance = new EventPipeline(
                e -> System.out.println(Event.dataToString(e.data(), 0) + "\n")
        );

        instance.addNode(
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

        instance.addNode(
                new Previous(
                        new RawInput("C")
                )
        );

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
}