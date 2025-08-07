package org.example;

import org.example.events.Event;
import org.example.nodesOld.OuterJoin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OuterJoinTest {

    @Test
    @DisplayName("OuterJoin driving Event")
    void testOuterJoinDrivingEvent() {
        var node = new OuterJoin("A", "B", "C");
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("OuterJoin other Event")
    void testOuterJoinOtherEvent() {
        var node = new OuterJoin("A", "B", "C");
        assertFalse(node.give(new Event<>("B", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("OuterJoin all Events")
    void testOuterJoinAllEvents() {
        var node = new OuterJoin("A", "B", "C");
        assertFalse(node.give(new Event<>("C", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("B", 0, 0)).isPresent());

        var resOpt = node.give(new Event<>("A", 0, 100));
        assertTrue(resOpt.isPresent());

        var result = resOpt.get();
        assertEquals(100, result.getTimestamp());
        assertTrue(result.getAllTypes().stream().anyMatch(s -> s.equals("OuterJoin(A, B, C)[" + node.hashCode() + "]")));
        assertArrayEquals(new Object[]{0, 0, 0}, ((Object[]) result.getValue("OuterJoin(A, B, C)[" + node.hashCode() + "]")));
    }

    @Test
    @DisplayName("OuterJoin foreign Event")
    void testOuterJoinForeignEvent() {
        var node = new OuterJoin("A", "B", "C");
        assertFalse(node.give(new Event<>("D", 0, 0)).isPresent());
    }
}
