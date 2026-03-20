package backend.academy.linktracker.scrapper.client.provider;

import backend.academy.linktracker.scrapper.client.provider.exception.ExternalClientException;
import backend.academy.linktracker.scrapper.client.provider.exception.ExternalClientHttpException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
public abstract class AbstractRestTrackedClient {

    protected <T> T getBody(
            RestClient.RequestHeadersSpec<?> requestSpec,
            Class<T> bodyType,
            URI link,
            String client,
            String operation) {
        try {
            T body = requestSpec
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new ExternalClientHttpException(
                                client, operation, link, response.getStatusCode(), readResponseBody(response));
                    })
                    .body(bodyType);

            if (body == null) {
                throw emptyResponse(client, operation, link, "Response body is empty");
            }

            return body;
        } catch (ExternalClientHttpException e) {
            log.atWarn()
                    .setCause(e)
                    .addKeyValue("client", client)
                    .addKeyValue("operation", operation)
                    .addKeyValue("link", link)
                    .addKeyValue("http_status", e.statusCode().value())
                    .addKeyValue("response_body", e.responseBody())
                    .log("Внешний сервис вернул ошибку");

            throw e;
        } catch (ExternalClientException e) {
            log.atWarn()
                    .setCause(e)
                    .addKeyValue("client", client)
                    .addKeyValue("operation", operation)
                    .addKeyValue("link", link)
                    .log("Внешний сервис вернул некорректный ответ");

            throw e;
        } catch (RestClientException e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("client", client)
                    .addKeyValue("operation", operation)
                    .addKeyValue("link", link)
                    .log("Ошибка вызова внешнего сервиса");

            throw new ExternalClientException(client, operation, link, "External client call failed", e);
        }
    }

    protected ExternalClientException emptyResponse(String client, String operation, URI link, String message) {
        return new ExternalClientException(client, operation, link, message);
    }

    private String readResponseBody(ClientHttpResponse response) {
        try {
            return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<unavailable>";
        }
    }
}
