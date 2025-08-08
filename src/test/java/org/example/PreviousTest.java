package org.example;

import org.example.events.Event;
import org.example.nodes.Previous;
import org.example.nodes.RawInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PreviousTest {

    @Test
    @DisplayName("Previous empty on first Event")
    void testPreviousEmptyOnFirstEvent() {
        var node = new Previous(new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).event().isPresent());
    }

    @Test
    @DisplayName("Previous")
    void testPreviousReturnEvent() {
        var node = new Previous(new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).event().isPresent());

        var optRes = node.give(new Event<>("A", 1, 50));
        assertTrue(optRes.event().isPresent());
        var result = optRes.event().get();

        assertEquals(50, result.timestamp());
        assertEquals(0, ((Map<String, ?>) result.data().get("prv")).get("A"));
    }

    @Test
    @DisplayName("Previous foreign Event")
    void testPreviousForeignEvent() {
        var node = new Previous(new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).event().isPresent());
        assertFalse(node.give(new Event<>("B", 1, 50)).event().isPresent());
    }
}
