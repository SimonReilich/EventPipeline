package org.example;

import org.example.events.Event;
import org.example.nodes.Previous;
import org.example.nodes.RawInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PreviousTest {

    @Test
    @DisplayName("Previous empty on first Event")
    void testPreviousEmptyOnFirstEvent() {
        var node = new Previous(new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("Previous")
    void testPreviousReturnEvent() {
        var node = new Previous(new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());

        var optRes = node.give(new Event<>("A", 1, 50));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(50, result.timestamp());
        assertEquals(0, result.getValue("A"));
    }

    @Test
    @DisplayName("Previous foreign Event")
    void testPreviousForeignEvent() {
        var node = new Previous(new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("B", 1, 50)).isPresent());
    }
}
