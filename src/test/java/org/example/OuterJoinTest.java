package org.example;

import org.example.events.Event;
import org.example.nodes.OuterJoin;
import org.example.nodes.RawInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OuterJoinTest {

    @Test
    @DisplayName("OuterJoin driving Event")
    void testOuterJoinDrivingEvent() {
        var node = new OuterJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertTrue(node.give(new Event<>("A", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("OuterJoin other Event")
    void testOuterJoinOtherEvent() {
        var node = new OuterJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertTrue(node.give(new Event<>("B", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("OuterJoin all Events")
    void testOuterJoinAllEvents() {
        var node = new OuterJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertTrue(node.give(new Event<>("C", 0, 0)).isPresent());
        assertTrue(node.give(new Event<>("B", 0, 0)).isPresent());

        var resOpt = node.give(new Event<>("A", 0, 100));
        assertTrue(resOpt.isPresent());

        var result = resOpt.get();
        assertEquals(100, result.timestamp());
        assertEquals(0, (Integer) result.data().get("A"));
    }

    @Test
    @DisplayName("OuterJoin foreign Event")
    void testOuterJoinForeignEvent() {
        var node = new OuterJoin(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("D", 0, 0)).isPresent());
    }
}
