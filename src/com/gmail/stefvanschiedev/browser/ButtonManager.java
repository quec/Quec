package com.gmail.stefvanschiedev.browser;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import java.util.*;

/**
 * A manager which contains pressed buttons for specific nodes
 */
class ButtonManager {

    private static final Map<Node, List<KeyCode>> KEYS = new HashMap<>();

    static void put(Node node, KeyCode code) {
        List<KeyCode> codes;

        if (KEYS.containsKey(node))
            codes = KEYS.get(node);
        else
            codes = new ArrayList<>();

        codes.add(code);
        KEYS.put(node, codes);
    }

    static void remove(Node node, KeyCode code) {
        List<KeyCode> codes = KEYS.get(node);

        if (codes == null)
            return;

        codes.remove(code);

        if (codes.isEmpty())
            KEYS.remove(node);
        else
            KEYS.put(node, codes);
    }

    static boolean contains(Node node, KeyCode code) {
        List<KeyCode> codes = KEYS.get(node);

        return codes != null && codes.contains(code);
    }
}