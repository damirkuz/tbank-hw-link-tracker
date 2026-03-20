package backend.academy.linktracker.scrapper.config.client.protocol;

import backend.academy.linktracker.bot.generated.client.DefaultApi;
import backend.academy.linktracker.scrapper.config.properties.BotProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class BotHttpClientConfig {

    @Bean
    RestClient botRestClient(RestClient.Builder restClientBuilder, BotProperties properties) {
        return restClientBuilder.baseUrl(properties.http().baseUrl()).build();
    }

    @Bean
    DefaultApi botDefaultApi(RestClient botRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(botRestClient))
                .build();

        return factory.createClient(DefaultApi.class);
    }
}
