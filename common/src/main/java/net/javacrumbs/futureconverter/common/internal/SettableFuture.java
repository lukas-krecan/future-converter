package net.javacrumbs.futureconverter.common.internal;

/**
 * Destination of events.
 * @param <T>
 */
public interface SettableFuture<T> {
    void setResult(T value);

    void setException(Throwable exception);

    void setCancellationCallback(Runnable callback);
}
