package org.example;

import org.example.events.Event;
import org.example.nodesOld.InnerJoin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InnerJoinTest {

    @Test
    @DisplayName("InnerJoin driving Event")
    void testInnerJoinDrivingEvent() {
        var node = new InnerJoin("A", "B", "C");
        assertTrue(node.give(new Event<>("A", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("InnerJoin other Event")
    void testInnerJoinOtherEvent() {
        var node = new InnerJoin("A", "B", "C");
        assertTrue(node.give(new Event<>("B", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("InnerJoin all Events")
    void testInnerJoinAllEvents() {
        var node = new InnerJoin("A", "B", "C");
        assertTrue(node.give(new Event<>("C", 0, 0)).isPresent());
        assertTrue(node.give(new Event<>("B", 0, 0)).isPresent());

        var resOpt = node.give(new Event<>("A", 0, 100));
        assertTrue(resOpt.isPresent());

        var result = resOpt.get();
        assertEquals(100, result.getTimestamp());
        assertTrue(result.getAllTypes().stream().anyMatch(s -> s.equals("InnerJoin(A, B, C)[" + node.hashCode() + "]")));
        assertArrayEquals(new Object[]{0, 0, 0}, ((Object[]) result.getValue("InnerJoin(A, B, C)[" + node.hashCode() + "]")));
    }

    @Test
    @DisplayName("InnerJoin foreign Event")
    void testInnerJoinForeignEvent() {
        var node = new InnerJoin("A", "B", "C");
        assertFalse(node.give(new Event<>("D", 0, 0)).isPresent());
    }
}
