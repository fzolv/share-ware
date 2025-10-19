package com.fzolv.shareware.hull.locks;

import java.util.concurrent.TimeUnit;

public interface AdaptableLockProvider {

    void lock(String key);

    boolean tryLock(String key, long time, TimeUnit unit) throws InterruptedException;

    void unlock(String key);
}


