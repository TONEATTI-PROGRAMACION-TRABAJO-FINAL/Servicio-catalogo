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
| `CATEDRA_SYNC_SNAPSHOT_ON_STARTUP` | `catedra.sync.snapshot-on-startup` | Opcional, por defecto `true` |
| `CATEDRA_SYNC_MAX_ATTEMPTS` | `catedra.sync.max-attempts` | Opcional, por defecto `8` intentos en total |
| `CATEDRA_SYNC_INITIAL_BACKOFF` | `catedra.sync.initial-backoff` | Opcional, por defecto `PT2S`; la espera se duplica en cada reintento |
| `CATEDRA_SYNC_MAX_BACKOFF` | `catedra.sync.max-backoff` | Opcional, por defecto `PT60S` |
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

Los campos `integration.*` los devuelven `POST /api/student/register`, `POST /api/authenticate` y `GET /api/student/integration`.

Salvo `CATALOGO_SERVER_PORT`, los timeouts y las `CATEDRA_SYNC_*`, todas son obligatorias. Si falta alguna, la aplicación no arranca y muestra `APPLICATION FAILED TO START` con la lista de propiedades faltantes.

### Sincronización del catálogo

Al arrancar, el servicio compara su versión local con las de la cátedra (secciones 14 y 18.2 del contrato):

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

Los tests no necesitan las variables reales. `src/test/resources/application-test.properties` define valores ficticios y el test de contexto activa el perfil `test`, así que `./mvnw clean verify` funciona igual en local y en GitHub Actions.
