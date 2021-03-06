package com.gmail.stefvanschiedev.browser.utils;

import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;

import java.util.*;

/**
 * A manager which handles key shortcuts
 */
public class KeyShortcutManager {

    private KeyShortcutManager() {}
    private static final KeyShortcutManager INSTANCE = new KeyShortcutManager();
    public static KeyShortcutManager getInstance() { return INSTANCE; }

    private final Map<List<KeyCode>, Task> keyshortcuts = new HashMap<>();
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public void add(List<KeyCode> codes, Task task) {
        keyshortcuts.put(codes, task);
    }

    public void initialize(Control control) {
        control.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            //check combos
            for (Map.Entry<List<KeyCode>, Task> list : keyshortcuts.entrySet()) {
                boolean found = true;

                for (KeyCode key : list.getKey()) {
                    if (!pressedKeys.contains(key)) {
                        found = false;
                        break;
                    }
                }

                if (found)
                    list.getValue().call();
            }
        });

        control.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
    }
}