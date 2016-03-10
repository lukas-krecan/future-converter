package net.javacrumbs.futureconverter.common.internal;

/**
 * Used for converting from Observable.
 * @param <T>
 */
public interface SettableFuture<T> extends OriginSource {
    void setResult(T value);

    void setException(Throwable exception);

    void setCancellationCallback(Runnable callback);
}
