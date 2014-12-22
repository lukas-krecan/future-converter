package net.javacrumbs.futureconverter.java8common;

import org.junit.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public abstract class AbstractCompletionStageTest {
    protected static final String VALUE = "test";
    protected static final String VALUE2 = "value2";
    protected static final RuntimeException EXCEPTION = new RuntimeException("Test");

    protected abstract CompletionStage<String> createCompletionStage();

    protected abstract CompletionStage<String> createOtherCompletionStage();

    protected abstract CompletionStage<String> createExceptionalCompletionStage();

    protected abstract void finishCalculation();

    protected abstract void finishCalculationExceptionally();

    @Test
    public void acceptShouldWork() {
        CompletionStage<String> completionStage = createCompletionStage();

        Consumer<String> consumer = mock(Consumer.class);
        completionStage.thenAccept(consumer);

        finishCalculation();

        verify(consumer).accept(VALUE);
    }

    @Test
    public void exceptionallyShouldTranslateExceptionToAValue() {
        CompletionStage<String> completionStage = createExceptionalCompletionStage();

        Consumer<String> consumer = mock(Consumer.class);
        Function<Throwable, String> function = mock(Function.class);
        when(function.apply(EXCEPTION)).thenReturn(VALUE);
        completionStage.exceptionally(function).thenAccept(consumer);

        finishCalculationExceptionally();

        verify(function, times(1)).apply(EXCEPTION);
        verify(consumer).accept(VALUE);
    }

    @Test
    public void exceptionallyShouldPassValue() {
        CompletionStage<String> completionStage = createCompletionStage();

        Consumer<String> consumer = mock(Consumer.class);
        Function<Throwable, String> function = mock(Function.class);
        when(function.apply(EXCEPTION)).thenReturn(VALUE);
        completionStage.exceptionally(function).thenAccept(consumer);

        finishCalculation();

        verifyZeroInteractions(function);
        verify(consumer).accept(VALUE);
    }

    @Test
    public void exceptionFromThenApplyShouldBePassedToTheNextPhase() {
        CompletionStage<String> completionStage = createCompletionStage();

        Function<String, Integer> conversion = mock(Function.class);
        Function<Throwable, Integer> errorHandler = mock(Function.class);
        when(errorHandler.apply(EXCEPTION)).thenReturn(null);
        when(conversion.apply(VALUE)).thenThrow(EXCEPTION);
        completionStage.thenApply(conversion).exceptionally(errorHandler);

        finishCalculation();

        verify(errorHandler).apply(isA(CompletionException.class));
        verify(conversion).apply(VALUE);
    }

    @Test
    public void shouldCombineValues() {
        CompletionStage<String> completionStage1 = createCompletionStage();
        CompletionStage<String> completionStage2 = createOtherCompletionStage();

        BiFunction<String, String, Integer> combiner = mock(BiFunction.class);
        Consumer<Integer> consumer = mock(Consumer.class);

        when(combiner.apply(VALUE, VALUE2)).thenReturn(5);

        completionStage1.thenCombine(completionStage2, combiner).thenAccept(consumer);
        finishCalculation();

        verify(combiner).apply(VALUE, VALUE2);
        verify(consumer).accept(5);
    }

    @Test
    public void shouldCombineValuesInOppositeOrder() {
        CompletionStage<String> completionStage1 = createOtherCompletionStage();
        CompletionStage<String> completionStage2 = createCompletionStage();

        BiFunction<String, String, Integer> combiner = mock(BiFunction.class);
        Consumer<Integer> consumer = mock(Consumer.class);

        when(combiner.apply(VALUE2, VALUE)).thenReturn(5);

        completionStage1.thenCombine(completionStage2, combiner).thenAccept(consumer);
        finishCalculation();

        verify(combiner).apply(VALUE2, VALUE);
        verify(consumer).accept(5);
    }


    @Test
    public void exceptionFromThenAcceptShouldBePassedToTheNextPhase() {
        CompletionStage<String> completionStage = createCompletionStage();

        Consumer<String> consumer = mock(Consumer.class);
        Function<Throwable, Void> errorHandler = mock(Function.class);
        when(errorHandler.apply(EXCEPTION)).thenReturn(null);
        doThrow(EXCEPTION).when(consumer).accept(VALUE);
        completionStage.thenAccept(consumer).exceptionally(errorHandler);

        finishCalculation();

        verify(errorHandler).apply(isA(CompletionException.class));
        verify(consumer).accept(VALUE);
    }

    @Test
    public void thenApplyShouldTransformTheValue() {
        CompletionStage<String> completionStage = createCompletionStage();

        Consumer<Integer> consumer = mock(Consumer.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).thenAccept(consumer);

        finishCalculation();

        verify(consumer).accept(8);
    }

    @Test
    public void shouldNotFailOnException() {
        CompletionStage<String> completionStage = createExceptionalCompletionStage();

        Consumer<Integer> consumer = mock(Consumer.class);
        Function<Throwable, Void> errorFunction = mock(Function.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).thenAccept(consumer).exceptionally(errorFunction);

        finishCalculationExceptionally();

        verifyZeroInteractions(consumer);
        verify(errorFunction, times(1)).apply(isA(CompletionException.class));
    }

    @Test
    public void whenCompleteShouldAcceptValue() {
        CompletionStage<String> completionStage = createCompletionStage();

        BiConsumer<Integer, Throwable> consumer = mock(BiConsumer.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).whenComplete(consumer);

        finishCalculation();

        verify(consumer).accept(8, null);
    }

    @Test
    public void whenCompleteShouldAcceptException() {
        CompletionStage<String> completionStage = createExceptionalCompletionStage();

        BiConsumer<Integer, Throwable> consumer = mock(BiConsumer.class);
        completionStage.thenApply(String::length).thenApply(i -> i * 2).whenComplete(consumer);

        finishCalculationExceptionally();

        verify(consumer).accept((Integer) isNull(), isA(CompletionException.class));
    }
}