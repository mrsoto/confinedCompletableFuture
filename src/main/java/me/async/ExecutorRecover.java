package me.async;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class ExecutorRecover {
    private final Executor mainExecutor;

    public ExecutorRecover(Executor mainExecutor) {
        this.mainExecutor = mainExecutor;
    }

    public <T> CompletionStage<T> compose(CompletionStage<T> other) {
        return other.thenApplyAsync(Function.identity(), mainExecutor);
    }
}
