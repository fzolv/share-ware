package com.fzolv.shareware.hull.locks;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LockManager {

    private final AdaptableLockProvider lockProvider;

    public LockManager(AdaptableLockProvider lockProvider) {
        this.lockProvider = lockProvider;
    }

    public void lock(String key) {
        lockProvider.lock(key);
    }

    public boolean tryLock(String key, long time, TimeUnit unit) throws InterruptedException {
        return lockProvider.tryLock(key, time, unit);
    }

    public void releaseLock(String key) {
        lockProvider.unlock(key);
    }
}


