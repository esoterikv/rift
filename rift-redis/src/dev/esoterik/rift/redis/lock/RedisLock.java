package dev.esoterik.rift.redis.lock;

import dev.esoterik.rift.lock.DistributedLock;
import dev.esoterik.rift.lock.RetryingException;
import dev.esoterik.rift.redis.map.KeyValue;
import dev.esoterik.rift.scheduler.Scheduler;
import java.time.Duration;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RedisLock implements DistributedLock {

  private final RedisLockWatcher watcher;
  private final RedisLockExecutor executor;

  private RedisLock(
      final Scheduler scheduler,
      final String key,
      final String identity,
      final Duration delay,
      final Duration until,
      final int tries,
      final KeyValue keyValue) {
    this.watcher = new RedisLockWatcher(scheduler, identity, key, until, keyValue);
    this.executor = new RedisLockExecutor(delay, until, tries);
  }

  public static DistributedLock create(
      final Scheduler scheduler,
      final String key,
      final String identity,
      final Duration delay,
      final Duration until,
      final int tries,
      final KeyValue keyValue) {
    return new RedisLock(scheduler, key, identity, delay, until, tries, keyValue);
  }

  @Override
  public <T> @Nullable T supply(
      final long currentThreadId, final @NotNull Supplier<@Nullable T> supplier)
      throws RetryingException {
    return executor.supply(
        () -> {
          boolean acquired = false;
          try {
            watcher.acquireOrThrow(currentThreadId);
            acquired = true;

            watcher.startWatching();
            try {
              return supplier.get();
            } finally {
              watcher.stopWatching();
            }
          } finally {
            if (acquired) {
              watcher.release(currentThreadId);
            }
          }
        });
  }

  @Override
  public void execute(final long currentThreadId, final @NotNull Runnable task)
      throws RetryingException {
    executor.execute(
        () -> {
          boolean acquired = false;
          try {
            watcher.acquireOrThrow(currentThreadId);
            acquired = true;

            watcher.startWatching();
            try {
              task.run();
            } finally {
              watcher.stopWatching();
            }
          } finally {
            if (acquired) {
              watcher.release(currentThreadId);
            }
          }
        });
  }

  @Override
  public boolean tryOnce(final long currentThreadId, final @NotNull Runnable task)
      throws RetryingException {
    return executor.supply(
        () -> {
          if (!watcher.acquire(currentThreadId)) {
            return false;
          }

          try {
            watcher.startWatching();
            try {
              task.run();
              return true;
            } finally {
              watcher.stopWatching();
            }
          } finally {
            watcher.release(currentThreadId);
          }
        });
  }

  @Override
  public void forceRelease() {
    watcher.forceRelease();
  }
}
