package com.toneatti.catalogo.synchronization.infrastructure.redis.adapter;

import com.toneatti.catalogo.config.CatedraIntegrationProperties;
import com.toneatti.catalogo.config.CatedraRedisConfig;
import com.toneatti.catalogo.professional.domain.model.Professional;
import com.toneatti.catalogo.professionalcategory.domain.model.ProfessionalCategory;
import com.toneatti.catalogo.synchronization.domain.exception.CatalogIncrementUnusableException;
import com.toneatti.catalogo.synchronization.domain.exception.CatedraUnavailableException;
import com.toneatti.catalogo.synchronization.domain.model.CatalogIncrement;
import com.toneatti.catalogo.synchronization.domain.model.CatalogVersionWindow;
import com.toneatti.catalogo.synchronization.infrastructure.shared.mapper.CatedraCatalogItemMapper;
import com.toneatti.catalogo.weeklyschedule.domain.model.WeeklySchedule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

/**
 * Against a real Redis whose integration user has the ACL of section 14.1: read commands on catedra:sync:* only.
 * The data is written by the default user. Skipped where Docker is not available.
 */
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(OutputCaptureExtension.class)
class RedisCatalogChangesRepositoryAdapterTest {

    private static final String USER = "grp_test";
    private static final String PASSWORD = "test-redis-password";
    private static final String NAMESPACE = "catedra:sync:*";
    private static final Duration COMMAND_TIMEOUT = Duration.ofMillis(300);
    private static final String CATEGORY_1 = """
            {"id":1,"name":"Clinica medica","description":"General","enabled":true,"source":"v1-compatible",
             "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"}""";
    private static final String PROFESSIONAL_15 = """
            {"id":15,"categoryId":1,"firstName":"Ana","lastName":"Gomez","enabled":false,
             "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"}""";
    private static final String PROFESSIONAL_16 = """
            {"id":16,"categoryId":1,"firstName":"Luis","lastName":"Perez","enabled":true,
             "createdAt":"2026-07-03T12:00:00Z","updatedAt":"2026-09-08T09:00:00Z"}""";
    private static final String SCHEDULE_21 = """
            {"id":21,"professionalId":15,"dayOfWeek":"FRIDAY","startTime":"14:00","endTime":"18:30:00",
             "slotDurationMinutes":20,"enabled":true,
             "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"}""";

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--user", USER, "on", ">" + PASSWORD, "~catedra:sync:*", "+@read",
                    "+@connection");

    private final List<LettuceConnectionFactory> factories = new ArrayList<>();
    private StringRedisTemplate seeder;
    private RedisCatalogChangesRepositoryAdapter adapter;

    @BeforeEach
    void connect() {
        // The default user has no password and every permission: it plays the cátedra publishing the catalog
        seeder = new StringRedisTemplate(factory("default", "unused", NAMESPACE, REDIS.getMappedPort(6379)));
        seeder.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushAll();
            connection.serverCommands().resetConfigStats();
            return null;
        });
        adapter = adapterFor(PASSWORD, NAMESPACE, REDIS.getMappedPort(6379));
    }

    @AfterEach
    void disconnect() {
        factories.forEach(LettuceConnectionFactory::destroy);
    }

    @Test
    void readsTheVersionWindow() {
        seedVersions("7", "4");

        assertThat(adapter.getVersionWindow()).isEqualTo(new CatalogVersionWindow(7, 4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "siete"})
    void failsAsUnusableWhenAVersionIsMissingOrNotDecimal(String current) {
        if (!current.isEmpty()) {
            seeder.opsForValue().set("catedra:sync:current-version", current);
        }
        seeder.opsForValue().set("catedra:sync:oldest-available-version", "4");

        assertThatThrownBy(adapter::getVersionWindow)
                .isInstanceOf(CatalogIncrementUnusableException.class)
                .hasMessageContaining("catedra:sync:current-version");
    }

    @Test
    void readsTheCurrentStateOfEveryChangedIdIncludingTheDisabledOnes() {
        seedChanges(7, "[1]", "[15,16]", "[21]");
        seedHash("professional-categories", Map.of("1", CATEGORY_1));
        seedHash("professionals", Map.of("15", PROFESSIONAL_15, "16", PROFESSIONAL_16));
        seedHash("weekly-schedules", Map.of("21", SCHEDULE_21));

        CatalogIncrement increment = adapter.getIncrement(7);

        assertThat(increment.version()).isEqualTo(7);
        assertThat(increment.professionalCategories()).extracting(ProfessionalCategory::getId,
                ProfessionalCategory::getName).containsExactly(tuple(1L, "Clinica medica"));
        assertThat(increment.professionals()).extracting(Professional::getId, Professional::getEnabled)
                .containsExactly(tuple(15L, false), tuple(16L, true));
        assertThat(increment.weeklySchedules()).singleElement().satisfies(schedule -> {
            assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
            assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(14, 0));
            assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(18, 30));
        });
    }

    @Test
    void doesNotReadTheHashesWhoseListIsEmpty() {
        seedChanges(7, "[]", "[15]", "[]");
        seedHash("professionals", Map.of("15", PROFESSIONAL_15));

        CatalogIncrement increment = adapter.getIncrement(7);

        assertThat(increment.professionalCategories()).isEmpty();
        assertThat(increment.weeklySchedules()).isEmpty();
        assertThat(hmgetCalls()).isEqualTo(1);
    }

    @Test
    void failsAsUnusableWhenTheChangesOfTheVersionAreMissing() {
        assertThatThrownBy(() -> adapter.getIncrement(5))
                .isInstanceOf(CatalogIncrementUnusableException.class)
                .hasMessageContaining("catedra:sync:changes:5 is missing");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not json",
            "{\"version\":6,\"changes\":{\"professionalCategories\":[],\"professionals\":[],\"weeklySchedules\":[]}}",
            "{\"version\":7}",
            "{\"version\":7,\"changes\":{\"professionalCategories\":[],\"professionals\":[]}}",
            "{\"version\":7,\"changes\":{\"professionalCategories\":[null],\"professionals\":[],\"weeklySchedules\":[]}}"
    })
    void failsAsUnusableWhenTheChangesCannotBeApplied(String changes) {
        seeder.opsForValue().set("catedra:sync:changes:7", changes);

        assertThatThrownBy(() -> adapter.getIncrement(7)).isInstanceOf(CatalogIncrementUnusableException.class);
    }

    @Test
    void failsAsUnusableWhenAChangedIdIsMissingFromItsHash() {
        seedChanges(7, "[]", "[15,16]", "[]");
        seedHash("professionals", Map.of("15", PROFESSIONAL_15));

        assertThatThrownBy(() -> adapter.getIncrement(7))
                .isInstanceOf(CatalogIncrementUnusableException.class)
                .hasMessageContaining("Id 16 is missing from catedra:sync:professionals");
    }

    // Unreadable JSON, the entity of another field, and a field of the wrong type
    @ParameterizedTest
    @ValueSource(strings = {"not json", "{\"id\":16,\"firstName\":\"Luis\"}", "{\"id\":15,\"createdAt\":\"ayer\"}"})
    void failsAsUnusableWhenAHashValueIsNotTheEntityOfItsField(String value) {
        seedChanges(7, "[]", "[15]", "[]");
        seedHash("professionals", Map.of("15", value));

        assertThatThrownBy(() -> adapter.getIncrement(7)).isInstanceOf(CatalogIncrementUnusableException.class);
    }

    @Test
    void failsAsUnavailableWhenTheConnectionIsRefused() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = socket.getLocalPort();
        }
        RedisCatalogChangesRepositoryAdapter unreachable = adapterFor(PASSWORD, NAMESPACE, closedPort);

        assertThatThrownBy(unreachable::getVersionWindow)
                .isInstanceOf(CatedraUnavailableException.class)
                .hasMessageContaining("Redis read failed: MGET");
    }

    @Test
    void failsAsUnavailableWhenACommandTakesLongerThanTheTimeout() {
        seedVersions("7", "4");
        adapter.getVersionWindow();
        Duration pause = COMMAND_TIMEOUT.multipliedBy(3);
        seeder.execute((RedisCallback<Void>) connection -> pauseClients(connection, pause));

        assertThatThrownBy(adapter::getVersionWindow).isInstanceOf(CatedraUnavailableException.class);
        // The pause also holds the next test's seeding, so it has to end first
        await().pollDelay(pause).atMost(pause.multipliedBy(2)).until(() -> true);
    }

    @Test
    void failsAsUnavailableOutsideTheNamespaceAllowedByTheAcl() {
        RedisCatalogChangesRepositoryAdapter outside = adapterFor(PASSWORD, "alumnos:otro-grupo:*",
                REDIS.getMappedPort(6379));

        assertThatThrownBy(outside::getVersionWindow).isInstanceOf(CatedraUnavailableException.class);
    }

    @Test
    void failsAsUnavailableWithAWrongPasswordWithoutLeakingIt(CapturedOutput output) {
        String wrongPassword = "wrong-redis-password";
        RedisCatalogChangesRepositoryAdapter rejected = adapterFor(wrongPassword, NAMESPACE,
                REDIS.getMappedPort(6379));

        assertThatThrownBy(rejected::getVersionWindow)
                .isInstanceOf(CatedraUnavailableException.class)
                .message().doesNotContain(wrongPassword);
        assertThat(output).doesNotContain(wrongPassword, PASSWORD);
    }

    private void seedVersions(String current, String oldest) {
        seeder.opsForValue().set("catedra:sync:current-version", current);
        seeder.opsForValue().set("catedra:sync:oldest-available-version", oldest);
    }

    private void seedChanges(long version, String categories, String professionals, String schedules) {
        seeder.opsForValue().set("catedra:sync:changes:" + version, "{\"version\":" + version + ",\"changes\":{"
                + "\"professionalCategories\":" + categories + ",\"professionals\":" + professionals
                + ",\"weeklySchedules\":" + schedules + "}}");
    }

    private void seedHash(String hash, Map<String, String> fields) {
        seeder.opsForHash().putAll("catedra:sync:" + hash, fields);
    }

    private long hmgetCalls() {
        Properties stats = seeder.execute((RedisCallback<Properties>) connection ->
                connection.serverCommands().info("commandstats"));
        String hmget = stats.getProperty("cmdstat_hmget");
        return hmget == null ? 0 : Long.parseLong(hmget.replaceAll("^calls=(\\d+).*$", "$1"));
    }

    private static Void pauseClients(RedisConnection connection, Duration duration) {
        connection.execute("CLIENT", "PAUSE".getBytes(), String.valueOf(duration.toMillis()).getBytes());
        return null;
    }

    private RedisCatalogChangesRepositoryAdapter adapterFor(String password, String namespace, int port) {
        CatedraIntegrationProperties properties = properties(USER, password, namespace, port);
        LettuceConnectionFactory factory = start(new CatedraRedisConfig().redisConnectionFactory(properties));
        return new RedisCatalogChangesRepositoryAdapter(new StringRedisTemplate(factory), JsonMapper.builder().build(),
                new CatedraCatalogItemMapper(), properties);
    }

    private LettuceConnectionFactory factory(String user, String password, String namespace, int port) {
        CatedraIntegrationProperties properties = properties(user, password, namespace, port);
        return start(new CatedraRedisConfig().redisConnectionFactory(properties));
    }

    private LettuceConnectionFactory start(LettuceConnectionFactory factory) {
        factory.afterPropertiesSet();
        factory.start();
        factories.add(factory);
        return factory;
    }

    private static CatedraIntegrationProperties properties(String user, String password, String namespace, int port) {
        return new CatedraIntegrationProperties("http://localhost:8089", "test-jwt", Duration.ofSeconds(1),
                Duration.ofSeconds(1),
                new CatedraIntegrationProperties.Redis(REDIS.getHost(), port, user, password, namespace,
                        Duration.ofSeconds(1), COMMAND_TIMEOUT),
                new CatedraIntegrationProperties.Kafka("localhost:9092", "alumnos-test", "catedra.catalog.test"));
    }
}
