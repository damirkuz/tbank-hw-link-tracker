package backend.academy.linktracker.scrapper.config.grpc;

import backend.academy.linktracker.scrapper.config.properties.BotProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotGrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    @ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
    public ManagedChannel botManagedChannel(BotProperties properties) {
        return ManagedChannelBuilder.forAddress(
                        properties.grpc().host(), properties.grpc().port())
                .usePlaintext()
                .build();
    }
}
