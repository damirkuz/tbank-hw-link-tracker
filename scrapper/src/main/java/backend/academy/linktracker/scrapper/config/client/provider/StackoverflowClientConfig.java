package backend.academy.linktracker.scrapper.config.client.provider;

import backend.academy.linktracker.scrapper.config.properties.StackoverflowProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class StackoverflowClientConfig {

    @Bean
    public RestClient stackoverflowRestClient(StackoverflowProperties properties) {
        var jdkHttpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        var requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(requestFactory)
                .build();
    }
}
