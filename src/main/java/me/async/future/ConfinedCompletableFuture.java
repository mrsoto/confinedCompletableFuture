package me.async.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>Completable future que garantiza que todas sus operaciones son ejecutadas sobre un
 * Executor asignado, igualmente garantiza que cuando se completa un CompletableStage
 * resume la ejecución sobre el Executor.</p>
 *
 * <p>Ejemplo de uso:<pre>{@code
 * var appExecutor = Executors.newWorkStealingPool();
 * var cef = new ConfinedCompletableFutureFactory(appExecutor);
 *
 * cef.kidnap(asyncRestGET())
 *  .thenApply(this::transform);
 * }</pre></p>
 * <p>
 * Contrario a lo que ocurre con un {@code CompletionStage}, el método transform
 * se ejecutará en executor {@code appExecutor}.</p>
 *
 * @param <T> tipo del contenido.
 * @see CompletableFuture#defaultExecutor()
 */
public class ConfinedCompletableFuture<T> extends CompletableFuture<T> {
    private final Executor mainExecutor;

    /**
     * Injección del Executor por constructor.
     *
     * @param mainExecutor al que estará confinado el flujo del completable future.
     */
    public ConfinedCompletableFuture(Executor mainExecutor) {
        this.mainExecutor = mainExecutor;
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new ConfinedCompletableFuture<>(mainExecutor);
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return super.thenComposeAsync(fn);
    }

    @Override
    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return super.thenApplyAsync(fn);
    }

    @Override
    public CompletableFuture<Void> thenRun(Runnable action) {
        return super.thenRunAsync(action);
    }

    @Override
    public <U, V> CompletableFuture<V> thenCombine(
            CompletionStage<? extends U> other,
            BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return super.thenCombineAsync(other, fn);
    }

    @Override
    public <U> CompletableFuture<Void> thenAcceptBoth(
            CompletionStage<? extends U> other,
            BiConsumer<? super T, ? super U> action
    ) {
        return super.thenAcceptBothAsync(other, action);
    }

    @Override
    public CompletableFuture<Void> runAfterBoth(
            CompletionStage<?> other,
            Runnable action
    ) {
        return super.runAfterBothAsync(other, action);
    }

    @Override
    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return super.whenCompleteAsync(action);
    }

    @Override
    public <U> CompletableFuture<U> applyToEither(
            CompletionStage<? extends T> other,
            Function<? super T, U> fn
    ) {
        return super.applyToEitherAsync(other, fn);
    }

    @Override
    public CompletableFuture<Void> runAfterEither(
            CompletionStage<?> other,
            Runnable action
    ) {
        return super.runAfterEitherAsync(other, action);
    }

    @Override
    public CompletableFuture<Void> acceptEither(
            CompletionStage<? extends T> other,
            Consumer<? super T> action
    ) {
        return super.acceptEitherAsync(other, action);
    }

    @Override
    public CompletableFuture<T> exceptionallyCompose(Function<Throwable, ? extends CompletionStage<T>> fn) {
        return super.exceptionallyComposeAsync(fn);
    }

    @Override
    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return super.exceptionallyAsync(fn);
    }

    @Override
    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return super.handleAsync(fn);
    }

    @Override
    public Executor defaultExecutor() {
        return mainExecutor;
    }
}
