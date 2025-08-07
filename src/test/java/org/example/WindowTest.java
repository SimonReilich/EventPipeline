package org.example;

import org.example.events.Event;
import org.example.nodesOld.Window;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WindowTest {

    @Test
    @DisplayName("Window single Event")
    void testWindowReturnEvent() {
        var node = new Window("A", 1, 2, Window.ITEM, Window.ITEM);
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());

        var optRes = node.give(new Event<>("A", 1, 50));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(50, result.getTimestamp());
        assertTrue(result.getAllTypes().stream().anyMatch(type -> type.equals("Window(A)[" + node.hashCode() + "]")));
        assertArrayEquals(new Object[]{1}, (Object[]) result.getValue("Window(A)[" + node.hashCode() + "]"));
    }

    @Test
    @DisplayName("Window foreign Event")
    void testWindowForeignEvent() {
        var node = new Window("A", 1, 1, Window.ITEM, Window.ITEM);
        assertTrue(node.give(new Event<>("A", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("B", 1, 50)).isPresent());
    }

    @Test
    @DisplayName("Window Size")
    void testWindowSize() {
        var node = new Window("A", 2, 3, Window.ITEM, Window.ITEM);
        assertFalse(node.give(new Event<>("A", 0, 0)).isPresent());
        assertFalse(node.give(new Event<>("A", 1, 25)).isPresent());

        var optRes = node.give(new Event<>("A", 2, 50));
        assertTrue(optRes.isPresent());
        var result = optRes.get();

        assertEquals(50, result.getTimestamp());
        assertTrue(result.getAllTypes().stream().anyMatch(type -> type.equals("Window(A)[" + node.hashCode() + "]")));
        assertEquals(2, ((Object[]) result.getValue("Window(A)[" + node.hashCode() + "]")).length);
    }
}
