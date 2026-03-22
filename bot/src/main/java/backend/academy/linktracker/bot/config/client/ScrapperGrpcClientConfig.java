package backend.academy.linktracker.bot.config.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrapperGrpcClientConfig {

    @Bean(destroyMethod = "shutdownNow")
    @ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "grpc")
    public ManagedChannel scrapperManagedChannel(
            @Value("${SCRAPPER_GRPC_HOST}") String scrapperHost, @Value("${SCRAPPER_GRPC_PORT}") Integer scrapperPort) {
        return ManagedChannelBuilder.forAddress(scrapperHost, scrapperPort)
                .usePlaintext()
                .build();
    }
}
