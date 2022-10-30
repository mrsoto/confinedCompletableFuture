package me.async;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public class Google implements AutoCloseable {

    private final Logger logger = Logger.getLogger(Google.class.getSimpleName());

    private final ObjectMapper om;

    private final CloseableHttpAsyncClient client;

    {
        client = HttpAsyncClients.createDefault();
        client.start();

        om = new ObjectMapper();
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private static HttpGet createRequest() {
        return new HttpGet(
                "https://streetviewpublish.googleapis.com/$discovery/rest?version=v1");
    }

    Services get() {
        logger.info("Current thread SYNC GET: %s".formatted(Thread.currentThread()));

        try (var client = HttpClientBuilder.create().build(); var response = client.execute(
                createRequest())) {

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("ERROR Getting service");
            }

            try (var content = response.getEntity().getContent(); var parser = om.createParser(
                    content)) {
                return om.readValue(parser, Services.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public CompletionStage<Services> asyncGet() {
        logger.info("Current thread asyncGET: %s".formatted(Thread.currentThread()));

        CompletableFuture<Services> future = new CompletableFuture<>();

        client.execute(createRequest(), new HttpResponseFutureCallback(future));
        return future;

    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private class HttpResponseFutureCallback implements FutureCallback<HttpResponse> {
        private final CompletableFuture<Services> future;

        public HttpResponseFutureCallback(CompletableFuture<Services> future) {
            this.future = future;
        }

        @Override
        public void completed(HttpResponse result) {
            if (result.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("ERROR Getting service");
            }
            try (var content = result.getEntity().getContent(); var parser = om.createParser(
                    content)) {
                future.complete(om.readValue(parser, Services.class));
            } catch (IOException e) {
                future.completeExceptionally(e);
            }

        }

        @Override
        public void failed(Exception ex) {
            future.completeExceptionally(ex);
        }

        @Override
        public void cancelled() {
            future.completeExceptionally(new RuntimeException("canceled"));
        }
    }
}
