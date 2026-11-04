<!-- calidad:inicio -->
![Calidad](https://img.shields.io/badge/Calidad-56%2F100-yellow) ![Aprobar](https://img.shields.io/badge/Aprobar-no-red) ![Destacar](https://img.shields.io/badge/Destacar-no-lightgrey) ![Build](https://img.shields.io/badge/Build-OK-green) ![Duplicación](https://img.shields.io/badge/Duplicaci%C3%B3n-39.9%25-orange) ![%POO](https://img.shields.io/badge/%25POO-72%25-yellow) ![Sueltas](https://img.shields.io/badge/Sueltas-0-green) ![D1](https://img.shields.io/badge/D1-9-red) ![D2](https://img.shields.io/badge/D2-64-yellow) ![D4](https://img.shields.io/badge/D4-0-red) ![D5](https://img.shields.io/badge/D5-95-green) ![D6](https://img.shields.io/badge/D6-100-green) ![D7](https://img.shields.io/badge/D7-62-yellow)

**Calidad (heurístico, 7 dimensiones):** índice **56/100** · build **OK** · aprobar **no (7/11)** · destacar **no**
Detalle completo en [`docs/EVALUACION.md`](docs/EVALUACION.md).
<!-- calidad:fin -->

# Servicio-catalogo

Copia local del catálogo de la cátedra (categorías, profesionales y horarios semanales), con una API de consulta para Servicio-turnos. El contrato de esa API está en [`docs/API-CATALOGO.md`](docs/API-CATALOGO.md).

## Configuración del entorno local

El servicio se integra con el servicio central de la cátedra (REST, Redis y Kafka). Los datos de conexión se leen **solo de variables de entorno**: no hay valores reales en el código ni en el repositorio.

El servicio escucha en el puerto **8081** (Servicio-turnos usa el 8080). Para cambiarlo, definir `CATALOGO_SERVER_PORT`.

### Variables

| Variable | Propiedad | De dónde sale |
| --- | --- | --- |
| `CATALOGO_SERVER_PORT` | `server.port` | Opcional, por defecto `8081` |
| `CATEDRA_API_BASE_URL` | `catedra.api-base-url` | URL base de la API REST, la envía la cátedra |
| `CATEDRA_JWT` | `catedra.jwt` | `id_token` de la cuenta técnica (registro o login) |
| `CATEDRA_CONNECT_TIMEOUT` | `catedra.connect-timeout` | Opcional, por defecto `PT2S` (duración ISO-8601) |
| `CATEDRA_READ_TIMEOUT` | `catedra.read-timeout` | Opcional, por defecto `PT10S`: el snapshot es la respuesta más pesada |
| `CATEDRA_SYNC_ON_STARTUP` | `catedra.sync.on-startup` | Opcional, por defecto `true`: sincroniza el catálogo al arrancar |
| `CATEDRA_SYNC_MAX_ATTEMPTS` | `catedra.sync.max-attempts` | Opcional, por defecto `8` intentos en total |
| `CATEDRA_SYNC_INITIAL_BACKOFF` | `catedra.sync.initial-backoff` | Opcional, por defecto `PT2S`; la espera se duplica en cada reintento |
| `CATEDRA_SYNC_MAX_BACKOFF` | `catedra.sync.max-backoff` | Opcional, por defecto `PT60S` |
| `CATEDRA_SYNC_POLL_ENABLED` | `catedra.sync.poll-enabled` | Opcional, por defecto `true` |
| `CATEDRA_SYNC_POLL_INTERVAL` | `catedra.sync.poll-interval` | Opcional, por defecto `PT1M` |
| `CATEDRA_REDIS_HOST` | `catedra.redis.host` | `integration.redisHost` |
| `CATEDRA_REDIS_PORT` | `catedra.redis.port` | `integration.redisPort` |
| `CATEDRA_REDIS_USERNAME` | `catedra.redis.username` | `integration.redisUsername` |
| `CATEDRA_REDIS_PASSWORD` | `catedra.redis.password` | `integration.redisPassword` |
| `CATEDRA_REDIS_READ_NAMESPACE` | `catedra.redis.read-namespace` | `integration.redisReadNamespace` (patrón terminado en `:*`, por ejemplo `catedra:sync:*`) |
| `CATEDRA_REDIS_CONNECT_TIMEOUT` | `catedra.redis.connect-timeout` | Opcional, por defecto `PT2S` |
| `CATEDRA_REDIS_COMMAND_TIMEOUT` | `catedra.redis.command-timeout` | Opcional, por defecto `PT2S` por comando |
| `CATEDRA_KAFKA_BOOTSTRAP_SERVERS` | `catedra.kafka.bootstrap-servers` | `integration.kafkaBootstrapServers` |
| `CATEDRA_KAFKA_CONSUMER_GROUP_ID` | `catedra.kafka.consumer-group-id` | `integration.kafkaConsumerGroupId` |
| `CATEDRA_KAFKA_CATALOG_TOPIC` | `catedra.kafka.catalog-topic` | `integration.kafkaCatalogTopic` |
| `CATEDRA_KAFKA_SECURITY_PROTOCOL` | `catedra.kafka.security-protocol` | Opcional. `PLAINTEXT` por defecto: el contrato no entrega credenciales de Kafka |
| `CATEDRA_KAFKA_AUTO_OFFSET_RESET` | `catedra.kafka.auto-offset-reset` | Opcional. `earliest` (por defecto) o `latest` |
| `CATEDRA_KAFKA_RETRY_MAX_ELAPSED_TIME` | `catedra.kafka.retry-max-elapsed-time` | Opcional. Cuánto se reintenta un `CatalogUpdated` cuyo procesamiento falla, en ISO-8601; por defecto `PT2M` |
| `CATEDRA_KAFKA_LISTENER_ENABLED` | `catedra.kafka.listener-enabled` | Opcional. `false` deja el consumidor detenido; por defecto `true` |
| `CATEDRA_KAFKA_PROCESSED_EVENTS_RETENTION` | `catedra.kafka.processed-events-retention` | Opcional, por defecto `P7D`: cuánto se guardan los `eventId` procesados para reconocer un `CatalogUpdated` repetido |

Los campos `integration.*` los devuelven `POST /api/student/register`, `POST /api/authenticate` y `GET /api/student/integration`.

Salvo `CATALOGO_SERVER_PORT`, los timeouts, las `CATEDRA_SYNC_*` y las `CATEDRA_KAFKA_*` marcadas como opcionales, todas son obligatorias. Si falta alguna, la aplicación no arranca y muestra `APPLICATION FAILED TO START` con la lista de propiedades faltantes.

### Sincronización del catálogo

Al arrancar, y después cada `CATEDRA_SYNC_POLL_INTERVAL` (por defecto un minuto), el servicio compara su versión local con las de la cátedra (secciones 14 y 18.2 del contrato). La comparación periódica es la que recupera el atraso si se pierde una notificación (sección 16); si falla, el siguiente intervalo es el reintento. Dos sincronizaciones nunca corren a la vez.

| Estado | Acción |
| --- | --- |
| Sin versión local (la base es en memoria, así que pasa en cada arranque) | Snapshot por REST, sin leer Redis |
| Local menor que `catedra:sync:oldest-available-version` | Snapshot (no se intenta `changes:{oldest}`) |
| Local mayor que `catedra:sync:current-version` | Snapshot, con un WARN: la cátedra reconstruyó su historial |
| Local igual a la actual | Nada |
| Otro caso | `changes:{local+1}` a `changes:{actual}` desde Redis, en orden |

El snapshot reemplaza la copia local con las tres colecciones y la versión en una sola transacción (`Catalog snapshot version N applied: ...`). Cada versión incremental se aplica en su propia transacción junto con el avance de la versión local (`Catalog version N applied: ...`); si una falla, no queda aplicada a medias y no se aplican las siguientes. Si los cambios de Redis no se pueden usar (falta `changes:{v}`, un id no está en su Hash o el JSON es inválido), se carga el snapshot una vez.

- Si la cátedra o Redis no responden (timeout, conexión rechazada, 5xx), el servicio arranca igual, escribe un WARN por intento y reintenta con espera exponencial hasta `CATEDRA_SYNC_MAX_ATTEMPTS`. Al rendirse escribe un ERROR. Mientras tanto la API devuelve la última copia local.
- Si responde 401 o 403, el JWT técnico no sirve: escribe un ERROR que pide revisar `CATEDRA_JWT` y no reintenta.

### Notificaciones de la cátedra (Kafka)

Además, el servicio consume `CatalogUpdated` de `CATEDRA_KAFKA_CATALOG_TOPIC` (sección 15.3 del contrato) y sincroniza enseguida, sin esperar el siguiente intervalo. El evento solo avisa: los datos salen de Redis o del snapshot, con las mismas reglas de la tabla de arriba. Como la sincronización compara versiones, un evento que llega después de uno perdido aplica también las versiones que faltaban.

- **Al menos una vez.** El offset se confirma recién después de procesar cada mensaje, nunca automáticamente. El `eventId` se guarda en `processed_catalog_events` (con restricción única) solo cuando la versión local llegó a la `newVersion` del evento: un mensaje repetido se reconoce y no hace nada (sección 18.1). Los `eventId` se guardan `CATEDRA_KAFKA_PROCESSED_EVENTS_RETENTION` (7 días por defecto); los más viejos se borran al registrar cada evento. La base es en memoria, así que la tabla empieza vacía en cada arranque: un mensaje que Kafka reentregue después de reiniciar vuelve a sincronizar, y eso no repite efectos porque solo se aplican las versiones que faltan.
- **Mensaje ilegible.** JSON inválido, sin `eventId` o `newVersion`, con un `eventType` distinto de `CatalogUpdated` o con un `schemaVersion` distinto de `1`: se deja en el log (`WARN`, con partición y offset) y se saltea, sin reintentar. Los campos desconocidos se ignoran.
- **Error al procesar.** Si la cátedra o Redis no responden, o si otra sincronización (el arranque o la comparación periódica) estaba corriendo y la versión local todavía no llegó a la del evento, el mensaje se reintenta en el lugar con espera exponencial (1 s, 2 s, 4 s… hasta 30 s entre intentos) durante `CATEDRA_KAFKA_RETRY_MAX_ELAPSED_TIME` (2 minutos por defecto). Mientras tanto la partición espera. Después se escribe un ERROR con el mensaje y se saltea; la comparación periódica recupera el atraso. Un 401 o 403 no se reintenta: escribe un ERROR que pide revisar `CATEDRA_JWT`.

### Pasos

1. Copiar el archivo de ejemplo y completar los valores reales:
   ```bash
   cp .env.example .env
   ```
2. Cargar las variables en la terminal y levantar el servicio:
   ```bash
   set -a; source .env; set +a
   ./mvnw spring-boot:run
   ```
   En IntelliJ también se pueden cargar desde *Run Configuration → Environment variables*.

El archivo `.env` contiene secretos (JWT y password de Redis): **nunca se commitea**, ni se pega en capturas, issues o logs.

### Tests

Los tests no necesitan las variables reales. `src/test/resources/application-test.properties` define valores ficticios y el test de contexto activa el perfil `test`, así que `./mvnw clean verify` funciona igual en local y en GitHub Actions. El consumidor Kafka queda detenido en los tests, salvo en su test de integración, que lo prueba contra un broker embebido (`@EmbeddedKafka`): no hace falta Docker ni el entorno de la cátedra.
