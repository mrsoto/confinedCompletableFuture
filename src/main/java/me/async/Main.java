package me.async;

import me.async.future.ConfinedCompletableFutureFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger;

    static {
        try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "logging.properties")) {
            if (resource != null) {
                LogManager.getLogManager().readConfiguration(resource);
            } else Logger.getGlobal().warning("logging.properties not found");
        } catch (IOException unused) {
            Logger.getGlobal().warning("logging.properties not found");
        }
        logger = Logger.getLogger(Main.class.getSimpleName());
    }

    public static void main(String... args) throws Exception {
        ConfinedCompletableFutureFactory cfFactory = getCfFactory();
        ExecutorService ioExecutor = createExecutor();

        try (var g = new Google()) {

            logger.info("Current thread 1: %s".formatted(Thread.currentThread()));

//        var service = getSync(g, ioExecutor)
            var service = cfFactory.confine(g.asyncGet().whenComplete((v, t) -> logger.info(
                            "async: v:[%s], t:[%s] [%s]".formatted(v,
                                    t,
                                    Thread.currentThread().getName()
                            )))).whenComplete((v, t) -> logger.info(
                            "kidnap CF: v:[%s], t:[%s] [%s]".formatted(v,
                                    t,
                                    Thread.currentThread().getName()
                            )))

                    .thenCombineAsync(getSync(g, ioExecutor),
                            Main::combineResult
                    ).toCompletableFuture().orTimeout(10,
                            TimeUnit.SECONDS
                    ).thenApply(services -> {
                        logger.info("Current thread 2: %s".formatted(Thread.currentThread()));
                        return services.toLowerCase(Locale.ROOT);
                    }).whenComplete((v, t) -> logger.info("Current thread 3: %s".formatted(Thread.currentThread())));

            logger.info("Current thread 4: %s".formatted(Thread.currentThread()));
            var sn = service.join();

            logger.info("Service: %s".formatted(sn));
        }

        logger.info("Current thread 5: %s".formatted(Thread.currentThread()));

        System.exit(0);
    }

    private static ExecutorService createExecutor() {
        var executorService = Executors.newWorkStealingPool(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();

            logger.info(executorService::toString);

        }));
        return executorService;
    }

    private static ConfinedCompletableFutureFactory getCfFactory() {
        return new ConfinedCompletableFutureFactory(Executors.newWorkStealingPool(
                1));
    }

    private static CompletionStage<Services> getSync(
            Google g, ExecutorService ioExecutor
    ) {
        return CompletableFuture.supplyAsync(
                g::get,
                ioExecutor
        ).whenComplete((v, t) -> logger.info("sync: v:[%s], t:[%s]".formatted(
                v,
                t
        )));
    }

    private static String combineResult(Services r1, Services r2) {
        return r1.canonicalName() + ":" + r2.canonicalName();
    }
}
