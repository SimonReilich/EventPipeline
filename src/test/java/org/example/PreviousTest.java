package org.example;

import org.example.events.Event;
import org.example.nodes.Previous;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PreviousTest {

    @Test
    @DisplayName("Previous empty on first Event")
    void testPreviousEmptyOnFirstEvent() {
        var node = new Previous("A");
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("Previous")
    void testPreviousReturnEvent() {
        var node = new Previous("A");
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());

        var optRes = node.give(new Event<>("A", 1, 50));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(50, result.getTimestamp());
        assertTrue(result.getAllTypes().stream().anyMatch(type -> type.equals("Previous(A)[" + node.hashCode() + "]")));
        assertEquals(0, result.getValue("Previous(A)[" + node.hashCode() + "]"));
    }

    @Test
    @DisplayName("Previous foreign Event")
    void testPreviousForeignEvent() {
        var node = new Previous("A");
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("B", 1, 50)).isPresent());
    }
}
