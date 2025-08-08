package org.example;

import org.example.events.Event;
import org.example.nodes.RawInput;
import org.example.nodes.Window;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WindowTest {

    @Test
    @DisplayName("Window single Event")
    void testWindowReturnEvent() {
        var node = new Window(Window.ITEM, 2, Window.ITEM, 1, new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).event().isPresent());

        var optRes = node.give(new Event<>("A", 1, 50));
        assertTrue(optRes.event().isPresent());
        var result = optRes.event().get();

        assertEquals(50, result.timestamp());
        assertArrayEquals(new Object[]{1}, (Object[]) result.data().get("w"));
    }

    @Test
    @DisplayName("Window foreign Event")
    void testWindowForeignEvent() {
        var node = new Window(Window.ITEM, 1, Window.ITEM, 1, new RawInput("A"));
        assertTrue(node.give(new Event<>("A", 0, 0)).event().isPresent());
        assertFalse(node.give(new Event<>("B", 1, 50)).event().isPresent());
    }

    @Test
    @DisplayName("Window Size")
    void testWindowSize() {
        var node = new Window(Window.ITEM, 3, Window.ITEM, 2, new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).event().isPresent());
        assertFalse(node.give(new Event<>("A", 1, 25)).event().isPresent());

        var optRes = node.give(new Event<>("A", 2, 50));
        assertTrue(optRes.event().isPresent());
        var result = optRes.event().get();

        assertEquals(50, result.timestamp());
        assertEquals(2, ((Object[]) result.getValue("w")).length);
    }
}
