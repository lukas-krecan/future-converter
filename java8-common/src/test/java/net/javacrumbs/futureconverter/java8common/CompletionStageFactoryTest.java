package net.javacrumbs.futureconverter.java8common;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CompletionStageFactoryTest {
    public static final String VALUE = "test";
    private final CompletionStageFactory factory = new CompletionStageFactory();

    @Test
    public void shouldCreateCompletableFuture() {
        ListenableFuture<String> listenableFuture = Futures.immediateFuture(VALUE);

        CompletionStage<String> completionStage = factory.createCompletableFuture((onSuccess, onError) -> {
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

        assertNotNull(completionStage);

        Consumer<String> consumer = mock(Consumer.class);
        completionStage.thenAccept(consumer);

        verify(consumer).accept(VALUE);
    }

}