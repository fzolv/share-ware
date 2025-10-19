package com.fzolv.shareware.hull.configs;

import com.fzolv.shareware.hull.locks.AdaptableLockProvider;
import com.fzolv.shareware.hull.locks.DatabaseLockProvider;
import com.fzolv.shareware.hull.locks.DistributedLockProvider;
import com.fzolv.shareware.hull.locks.InMemoryLockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LockConfiguration {

    @Bean
    @Primary
    public AdaptableLockProvider adaptableLockProvider(
            @Value("${shareware.locks.provider:in-memory}") String provider,
            InMemoryLockProvider inMemoryLockProvider,
            DatabaseLockProvider databaseLockProvider,
            DistributedLockProvider distributedLockProvider) {
        switch (provider.toLowerCase()) {
            case "db":
            case "database":
                return databaseLockProvider;
            case "dist":
            case "distributed":
                return distributedLockProvider;
            case "mem":
            case "in-memory":
            default:
                return inMemoryLockProvider;
        }
    }
}


