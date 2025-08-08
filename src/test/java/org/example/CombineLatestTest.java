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

        assertTrue(node.give(new Event<>("B", -1, 0)).event().isPresent());
        assertTrue(node.give(new Event<>("C", 1, 50)).event().isPresent());
        assertTrue(node.give(new Event<>("B", 1, 50)).event().isPresent());

        var optRes = node.give(new Event<>("A", 1, 100));
        assertTrue(optRes.event().isPresent());
        var result = optRes.event().get();

        assertEquals(100, result.timestamp());
        assertEquals(3, (((Map<String, ?>) result.data().get("cl")).size()));
    }

    @Test
    @DisplayName("CombineLatest Event")
    void testCombineLatestDrivingEvent() {
        var node = new CombineLatest(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertTrue(node.give(new Event<>("A", 0, 0)).event().isPresent());
    }

    @Test
    @DisplayName("CombineLatest foreign Event")
    void testCombineLatestForeignEvent() {
        var node = new CombineLatest(new RawInput("A"), new RawInput("B"), new RawInput("C"));
        assertFalse(node.give(new Event<>("D", 0, 0)).event().isPresent());
    }
}
