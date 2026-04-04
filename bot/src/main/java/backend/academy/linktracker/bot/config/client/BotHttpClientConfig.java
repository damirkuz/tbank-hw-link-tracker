package backend.academy.linktracker.bot.config.client;

import backend.academy.linktracker.bot.config.properties.ScrapperProperties;
import backend.academy.linktracker.scrapper.generated.client.DefaultApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "http")
public class BotHttpClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder restClientBuilder, ScrapperProperties scrapperProperties) {
        return restClientBuilder.baseUrl(scrapperProperties.httpUrl()).build();
    }

    @Bean
    DefaultApi botDefaultApi(RestClient botRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(botRestClient))
                .build();

        return factory.createClient(DefaultApi.class);
    }
}
