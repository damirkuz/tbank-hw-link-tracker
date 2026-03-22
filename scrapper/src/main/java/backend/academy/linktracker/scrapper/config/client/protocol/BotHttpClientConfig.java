package backend.academy.linktracker.scrapper.config.client.protocol;

import backend.academy.linktracker.bot.generated.client.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class BotHttpClientConfig {

    @Bean
    RestClient botRestClient(RestClient.Builder restClientBuilder, @Value("${BOT_HTTP_URL}") String botUrl) {
        return restClientBuilder.baseUrl(botUrl).build();
    }

    @Bean
    DefaultApi botDefaultApi(RestClient botRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(botRestClient))
                .build();

        return factory.createClient(DefaultApi.class);
    }
}
