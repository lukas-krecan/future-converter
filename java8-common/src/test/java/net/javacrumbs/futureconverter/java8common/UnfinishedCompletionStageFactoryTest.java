package net.javacrumbs.futureconverter.java8common;

import java.util.concurrent.CompletionStage;

public class UnfinishedCompletionStageFactoryTest extends AbstractCompletionStageTest {
    private final CompletionStageFactory factory = new CompletionStageFactory();
    private final DefaultListenable<String> defaultListenable = new DefaultListenable<>();

    protected CompletionStage<String> createCompletionStage() {
        return factory.createCompletableFuture(defaultListenable);
    }

    protected CompletionStage<String> createExceptionalCompletionStage() {
        return createCompletionStage();
    }

    @Override
    protected void finishCalculation() {
        defaultListenable.success(VALUE);
    }

    @Override
    protected void finishCalculationExceptionally() {
        defaultListenable.failure(EXCEPTION);
    }


}