package net.javacrumbs.futureconverter.java8common;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CompletionStageFactoryTest {
    public static final String VALUE = "test";
    public static final RuntimeException EXCEPTION = new RuntimeException("Test");
    private final CompletionStageFactory factory = new CompletionStageFactory();

    @Test
    public void acceptShouldWork() {
        ListenableFuture<String> listenableFuture = Futures.immediateFuture(VALUE);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        Consumer<String> consumer = mock(Consumer.class);
        completionStage.thenAccept(consumer);
        verify(consumer).accept(VALUE);
    }

    @Test
    public void exceptionallyShouldTranslateExceptionToAValue() {
        ListenableFuture<String> listenableFuture = Futures.immediateFailedFuture(EXCEPTION);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        Consumer<String> consumer = mock(Consumer.class);
        Function<Throwable, String> function = mock(Function.class);
        when(function.apply(EXCEPTION)).thenReturn(VALUE);
        completionStage.exceptionally(function).thenAccept(consumer);
        verify(function).apply(EXCEPTION);
        verify(consumer).accept(VALUE);
    }

    @Test
    public void exceptionallyShouldPassValue() {
        ListenableFuture<String> listenableFuture = Futures.immediateFuture(VALUE);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        Consumer<String> consumer = mock(Consumer.class);
        Function<Throwable, String> function = mock(Function.class);
        when(function.apply(EXCEPTION)).thenReturn(VALUE);
        completionStage.exceptionally(function).thenAccept(consumer);
        verifyZeroInteractions(function);
        verify(consumer).accept(VALUE);
    }

    @Test
    public void thenApplyShouldTransformTheValue() {
        ListenableFuture<String> listenableFuture = Futures.immediateFuture(VALUE);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        Consumer<Integer> consumer = mock(Consumer.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).thenAccept(consumer);
        verify(consumer).accept(8);
    }

    @Test
    public void shouldNotFailOnException() {
        ListenableFuture<String> listenableFuture = Futures.immediateFailedFuture(EXCEPTION);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        Consumer<Integer> consumer = mock(Consumer.class);
        Function<Throwable, Void> errorFunction = mock(Function.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).thenAccept(consumer).exceptionally(errorFunction);
        verifyZeroInteractions(consumer);
        verify(errorFunction).apply(isA(CompletionException.class));
    }

    @Test
    public void whenCompleteShouldAcceptValue() {
        ListenableFuture<String> listenableFuture = Futures.immediateFuture(VALUE);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        BiConsumer<Integer, Throwable> consumer = mock(BiConsumer.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).whenComplete(consumer);
        verify(consumer).accept(8, null);
    }

    @Test
    public void whenCompleteShouldAcceptException() {
        ListenableFuture<String> listenableFuture = Futures.immediateFailedFuture(EXCEPTION);

        CompletionStage<String> completionStage = createCompletionStage(listenableFuture);

        BiConsumer<Integer, Throwable> consumer = mock(BiConsumer.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).whenComplete(consumer);
        verify(consumer).accept((Integer) isNull(), isA(CompletionException.class));
    }

    private CompletionStage<String> createCompletionStage(ListenableFuture<String> listenableFuture) {
        return factory.createCompletableFuture((onSuccess, onError) -> {
            Futures.addCallback(listenableFuture, new FutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    onSuccess.accept(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    onError.accept(t);
                }
            });
        });
    }

}