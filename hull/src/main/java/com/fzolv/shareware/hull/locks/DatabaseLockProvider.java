package com.fzolv.shareware.hull.locks;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DatabaseLockProvider implements AdaptableLockProvider {

    @Override
    public void lock(String key) {
        throw new UnsupportedOperationException("DatabaseLockProvider not implemented yet");
    }

    @Override
    public boolean tryLock(String key, long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("DatabaseLockProvider not implemented yet");
    }

    @Override
    public void unlock(String key) {
        throw new UnsupportedOperationException("DatabaseLockProvider not implemented yet");
    }
}


