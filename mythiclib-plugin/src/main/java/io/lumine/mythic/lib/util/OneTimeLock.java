package io.lumine.mythic.lib.util;

import org.jetbrains.annotations.NotNull;

public class OneTimeLock {
    private boolean lock;

    public boolean isLocked() {
        return lock;
    }

    public void lock(@NotNull String errorMessage) {
        if (lock) throw new RuntimeException(errorMessage);
        lock = true;
    }
}
