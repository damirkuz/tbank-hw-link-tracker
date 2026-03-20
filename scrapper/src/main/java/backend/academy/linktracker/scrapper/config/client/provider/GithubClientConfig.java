package backend.academy.linktracker.scrapper.config.client.provider;

import backend.academy.linktracker.scrapper.config.properties.GithubProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GithubClientConfig {

    @Bean
    RestClient githubRestClient(RestClient.Builder restClientBuilder, GithubProperties properties) {
        var jdkHttpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

        var requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Accept", "application/vnd.github+json")
                .requestFactory(requestFactory)
                .build();
    }
}
