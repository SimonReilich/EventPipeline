package org.example;

import org.example.events.Event;
import org.example.nodes.InnerJoin;
import org.example.nodes.RawInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class InnerJoinTest {

    @Test
    @DisplayName("InnerJoin driving Event")
    void testInnerJoinDrivingEvent() {
        var node = new InnerJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("A", 0, 0)).event().isPresent());
    }

    @Test
    @DisplayName("InnerJoin other Event")
    void testInnerJoinOtherEvent() {
        var node = new InnerJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("B", 0, 0)).event().isPresent());
    }

    @Test
    @DisplayName("InnerJoin all Events")
    void testInnerJoinAllEvents() {
        var node = new InnerJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("C", 0, 0)).event().isPresent());
        assertFalse(node.give(new Event<>("B", 0, 0)).event().isPresent());

        var resOpt = node.give(new Event<>("A", 0, 100));
        assertFalse(resOpt.event().isPresent());
    }

    @Test
    @DisplayName("InnerJoin foreign Event")
    void testInnerJoinForeignEvent() {
        var node = new InnerJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("D", 0, 0)).event().isPresent());
    }
}
