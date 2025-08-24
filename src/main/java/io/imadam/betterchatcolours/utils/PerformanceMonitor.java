package io.imadam.betterchatcolours.utils;

import io.imadam.betterchatcolours.BetterChatColours;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance monitoring and metrics collection utility
 */
public class PerformanceMonitor {

  private final BetterChatColours plugin;
  private final ConcurrentHashMap<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, AtomicLong> operationTimes = new ConcurrentHashMap<>();
  private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

  public PerformanceMonitor(BetterChatColours plugin) {
    this.plugin = plugin;
  }

  /**
   * Start timing an operation
   */
  public OperationTimer startTimer(String operation) {
    return new OperationTimer(operation);
  }

  /**
   * Record an operation completion
   */
  public void recordOperation(String operation, long timeMs) {
    operationCounts.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
    operationTimes.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(timeMs);

    // Log slow operations
    if (timeMs > 100) {
      plugin.getLogger().warning("Slow operation detected: " + operation + " took " + timeMs + "ms");
    }
  }

  /**
   * Log current performance statistics
   */
  public void logStatistics() {
    plugin.getLogger().info("=== Performance Statistics ===");
    plugin.getLogger().info("Time: " + LocalDateTime.now().format(timeFormatter));
    plugin.getLogger().info("Online Players: " + Bukkit.getOnlinePlayers().size());

    for (String operation : operationCounts.keySet()) {
      long count = operationCounts.get(operation).get();
      long totalTime = operationTimes.get(operation).get();
      long avgTime = count > 0 ? totalTime / count : 0;

      plugin.getLogger().info(String.format(
          "  %s: %d operations, avg %dms",
          operation, count, avgTime));
    }

    // Memory usage
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;

    plugin.getLogger().info(String.format(
        "Memory: %dMB used / %dMB total",
        usedMemory / 1024 / 1024,
        totalMemory / 1024 / 1024));

    plugin.getLogger().info("===============================");
  }

  /**
   * Reset all statistics
   */
  public void reset() {
    operationCounts.clear();
    operationTimes.clear();
    plugin.getLogger().info("Performance statistics reset");
  }

  /**
   * Timer for tracking operation duration
   */
  public class OperationTimer implements AutoCloseable {
    private final String operation;
    private final long startTime;

    public OperationTimer(String operation) {
      this.operation = operation;
      this.startTime = System.currentTimeMillis();
    }

    @Override
    public void close() {
      long duration = System.currentTimeMillis() - startTime;
      recordOperation(operation, duration);
    }
  }
}
