package backend.academy.linktracker.scrapper.config.client.protocol;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotGrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    @ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
    public ManagedChannel botManagedChannel(
            @Value("${BOT_GRPC_HOST}") String botHost, @Value("${BOT_GRPC_PORT}") Integer botPort) {
        return ManagedChannelBuilder.forAddress(botHost, botPort).usePlaintext().build();
    }
}
