package com.aquarell.infrastructure.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Storage<K, V> {
    private static final byte MAX_NUM_SHARDS = 8;
    private final List<Shard<K, V>> values;
    private int shardCount;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Storage() {
        this.shardCount = MAX_NUM_SHARDS;
        this(MAX_NUM_SHARDS);
    }

    Storage(int shardCount) {
        if (shardCount <= 0) throw new IllegalArgumentException("Shard count must be positive");
        if ((shardCount & (shardCount - 1)) != 0) throw new IllegalArgumentException("Shard count must be a power of two");
        this.shardCount = shardCount;
        this.values = new ArrayList<>(this.shardCount);
        for (int i = 0; i < this.shardCount; i++) {
            this.values.add(new Shard<>(new ReentrantReadWriteLock(), new HashMap<>()));
        }
    }

    private HashMap<K, V> getBucket(K key) {
        var shard = this.values.get(key.hashCode() & (shardCount - 1));
        return shard.value();
    }

    public V get(K key) {
        lock.readLock().lock();

        try {
            return getBucket(key).get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public V add(K key, V value) {
        lock.writeLock().lock();

        try {
            return getBucket(key).put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
