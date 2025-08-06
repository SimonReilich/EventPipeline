package org.example;

import org.example.events.Event;

import java.util.PriorityQueue;

public class EventQueue extends PriorityQueue<Event<?>> {

    @Override
    public boolean add(Event<?> event) {
        boolean res = super.add(event);
        Main.notifyExecutor();
        return res;
    }
}
