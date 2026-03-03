package com.aquarell.infrastructure.storage;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;

public record Shard<K, V>(ReadWriteLock lock, HashMap<K, V> value) {}
