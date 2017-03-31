package com.gmail.stefvanschiedev.browser;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import java.util.*;

/**
 * A manager which contains pressed buttons for specific nodes
 */
class ButtonManager {

    private static Map<Node, List<KeyCode>> keys = new HashMap<>();

    static void put(Node node, KeyCode code) {
        List<KeyCode> codes;

        if (keys.containsKey(node))
            codes = keys.get(node);
        else
            codes = new ArrayList<>();

        codes.add(code);
        keys.put(node, codes);
    }

    static void remove(Node node, KeyCode code) {
        List<KeyCode> codes = keys.get(node);

        if (codes == null)
            return;

        codes.remove(code);

        if (codes.isEmpty())
            keys.remove(node);
        else
            keys.put(node, codes);
    }

    static boolean contains(Node node, KeyCode code) {
        List<KeyCode> codes = keys.get(node);

        return codes != null && codes.contains(code);
    }
}