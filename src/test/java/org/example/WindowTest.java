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
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());

        var optRes = node.give(new Event<>("A", 1, 50));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(50, result.getTimestamp());
        assertArrayEquals(new Object[]{1}, (Object[]) result.getData().get("A list"));
    }

    @Test
    @DisplayName("Window foreign Event")
    void testWindowForeignEvent() {
        var node = new Window(Window.ITEM, 1, Window.ITEM, 1, new RawInput("A"));
        assertTrue(node.give(new Event<>("A", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("B", 1, 50)).isPresent());
    }

    @Test
    @DisplayName("Window Size")
    void testWindowSize() {
        var node = new Window(Window.ITEM, 3, Window.ITEM, 2, new RawInput("A"));
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("A", 1, 25)).isPresent());

        var optRes = node.give(new Event<>("A", 2, 50));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(50, result.getTimestamp());
        assertEquals(2, ((Object[]) result.getValue("A list")).length);
    }
}
