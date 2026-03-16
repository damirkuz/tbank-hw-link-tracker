package backend.academy.linktracker.bot.config.grpc;

import backend.academy.linktracker.bot.config.properties.ScrapperProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrapperGrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    @ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "grpc")
    public ManagedChannel scrapperManagedChannel(ScrapperProperties properties) {
        return ManagedChannelBuilder.forAddress(
                        properties.grpc().host(), properties.grpc().port())
                .usePlaintext()
                .build();
    }
}
