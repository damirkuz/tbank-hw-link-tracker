package backend.academy.linktracker.scrapper.config.client.protocol;

import backend.academy.linktracker.scrapper.config.properties.BotProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
public class BotGrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel botManagedChannel(BotProperties botProperties) {
        return ManagedChannelBuilder.forAddress(botProperties.grpcHost(), botProperties.grpcPort())
                .usePlaintext()
                .build();
    }
}
