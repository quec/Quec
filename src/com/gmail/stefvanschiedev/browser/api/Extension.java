package com.gmail.stefvanschiedev.browser.api;

import com.gmail.stefvanschiedev.browser.BrowserTab;
import javafx.scene.Node;

import java.util.Collection;

public abstract class Extension {

    private boolean enabled;

    public abstract void onEnable();

    public abstract void onDisable();

    public abstract Collection<Node> getIcons(BrowserTab tab);

    public void setEnabled(boolean enable) {
        this.enabled = enable;

        if (enable)
            onEnable();
        else
            onDisable();
    }
}
