package com.fzolv.shareware.hull.locks;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class InMemoryLockProvider implements AdaptableLockProvider {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private ReentrantLock get(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    @Override
    public void lock(String key) {
        get(key).lock();
    }

    @Override
    public boolean tryLock(String key, long time, TimeUnit unit) throws InterruptedException {
        return get(key).tryLock(time, unit);
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = get(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}


