package backend.academy.linktracker.e2e;

import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractHttpE2EIT {

    protected static final Network NETWORK = Network.newNetwork();

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("link_tracker")
            .withUsername("postgres")
            .withPassword("postgres")
            .withNetwork(NETWORK)
            .withNetworkAliases("postgres")
            .withStartupTimeout(Duration.ofMinutes(2));

    protected static final GenericContainer<?> BOT = new GenericContainer<>(
                    new ImageFromDockerfile("link-tracker-bot-it", false)
                            .withDockerfile(Path.of("../bot/Dockerfile"))
                            .withFileFromPath(".", Path.of("..")))
            .withExposedPorts(8080)
            .withNetwork(NETWORK)
            .withNetworkAliases("bot")
            .withEnv("SERVER_PORT", "8080")
            .withEnv("SCRAPPER_TRANSPORT", "http")
            .withEnv("SCRAPPER_HTTP_BASE_URL", "http://scrapper:8081")
            .withEnv("TELEGRAM_BOT_TOKEN", "test-token")
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(3));

    protected static final GenericContainer<?> SCRAPPER = new GenericContainer<>(
                    new ImageFromDockerfile("link-tracker-scrapper-it", false)
                            .withDockerfile(Path.of("../scrapper/Dockerfile"))
                            .withFileFromPath(".", Path.of("..")))
            .withExposedPorts(8081)
            .withNetwork(NETWORK)
            .withNetworkAliases("scrapper")
            .withEnv("SERVER_PORT", "8081")
            .withEnv("BOT_TRANSPORT", "http")
            .withEnv("BOT_HTTP_BASE_URL", "http://bot:8080")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/link_tracker")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "postgres")
            .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(3));

    @BeforeAll
    static void beforeAll() {
        POSTGRES.start();
        BOT.start();
        SCRAPPER.start();
    }

    protected String botUrl() {
        return "http://" + BOT.getHost() + ":" + BOT.getMappedPort(8080);
    }

    protected String scrapperUrl() {
        return "http://" + SCRAPPER.getHost() + ":" + SCRAPPER.getMappedPort(8081);
    }
}
