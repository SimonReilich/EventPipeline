package org.example;

import org.example.events.Event;
import org.example.nodes.CombineLatest;
import org.example.nodes.RawInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CombineLatestTest {

    @Test
    @DisplayName("CombineLatest")
    void testCombineLatest() {
        var node = new CombineLatest(new RawInput("A"), new RawInput("B"), new RawInput("C"));

        assertTrue(node.give(new Event<>("B", -1, 0)).isPresent());
        assertTrue(node.give(new Event<>("C", 1, 50)).isPresent());
        assertTrue(node.give(new Event<>("B", 1, 50)).isPresent());

        var optRes = node.give(new Event<>("A", 1, 100));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(100, result.timestamp());
        assertArrayEquals(new Object[]{Map.entry("A", 1), Map.entry("B", 1), Map.entry("C", 1)}, (result.getDataSet().toArray()));
    }

    @Test
    @DisplayName("CombineLatest Event")
    void testCombineLatestDrivingEvent() {
        var node = new CombineLatest(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertTrue(node.give(new Event<>("A", 0, 0)).isPresent());
    }

    @Test
    @DisplayName("CombineLatest foreign Event")
    void testCombineLatestForeignEvent() {
        var node = new CombineLatest(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("D", 0, 0)).isPresent());
    }
}
