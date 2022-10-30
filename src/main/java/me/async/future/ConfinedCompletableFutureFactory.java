package me.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class ConfinedCompletableFutureFactory {
    private final Executor appExecutor;

    public ConfinedCompletableFutureFactory(Executor appExecutor) {
        this.appExecutor = appExecutor;
    }

    public <T> CompletableFuture<T> of() {
        return new ConfinedCompletableFuture<>(appExecutor);
    }

    public <T> CompletionStage<T> confine(CompletionStage<T> completionStage) {
        CompletableFuture<T> cf = new ConfinedCompletableFuture<>(appExecutor);

        completionStage.whenCompleteAsync((fv, throwable) -> {
            if (throwable != null) {
                cf.completeExceptionally(throwable);
            } else {
                cf.complete(fv);
            }
        }, appExecutor);

        return cf;
    }
}
