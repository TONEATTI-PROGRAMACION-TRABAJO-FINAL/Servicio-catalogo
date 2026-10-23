<!-- calidad:inicio -->
![Calidad](https://img.shields.io/badge/Calidad-57%2F100-yellow) ![Aprobar](https://img.shields.io/badge/Aprobar-no-red) ![Destacar](https://img.shields.io/badge/Destacar-no-lightgrey) ![Build](https://img.shields.io/badge/Build-OK-green) ![Duplicación](https://img.shields.io/badge/Duplicaci%C3%B3n-6.4%25-orange) ![%POO](https://img.shields.io/badge/%25POO-72%25-yellow) ![Sueltas](https://img.shields.io/badge/Sueltas-0-green) ![D1](https://img.shields.io/badge/D1-15-red) ![D2](https://img.shields.io/badge/D2-64-yellow) ![D4](https://img.shields.io/badge/D4-0-red) ![D5](https://img.shields.io/badge/D5-95-green) ![D6](https://img.shields.io/badge/D6-100-green) ![D7](https://img.shields.io/badge/D7-62-yellow)

**Calidad (heurístico, 7 dimensiones):** índice **57/100** · build **OK** · aprobar **no (7/11)** · destacar **no**
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
| `CATEDRA_REDIS_HOST` | `catedra.redis.host` | `integration.redisHost` |
| `CATEDRA_REDIS_PORT` | `catedra.redis.port` | `integration.redisPort` |
| `CATEDRA_REDIS_USERNAME` | `catedra.redis.username` | `integration.redisUsername` |
| `CATEDRA_REDIS_PASSWORD` | `catedra.redis.password` | `integration.redisPassword` |
| `CATEDRA_REDIS_READ_NAMESPACE` | `catedra.redis.read-namespace` | `integration.redisReadNamespace` |
| `CATEDRA_KAFKA_BOOTSTRAP_SERVERS` | `catedra.kafka.bootstrap-servers` | `integration.kafkaBootstrapServers` |
| `CATEDRA_KAFKA_CONSUMER_GROUP_ID` | `catedra.kafka.consumer-group-id` | `integration.kafkaConsumerGroupId` |
| `CATEDRA_KAFKA_CATALOG_TOPIC` | `catedra.kafka.catalog-topic` | `integration.kafkaCatalogTopic` |

Los campos `integration.*` los devuelven `POST /api/student/register`, `POST /api/authenticate` y `GET /api/student/integration`.

Salvo `CATALOGO_SERVER_PORT`, todas son obligatorias. Si falta alguna, la aplicación no arranca y muestra `APPLICATION FAILED TO START` con la lista de propiedades faltantes.

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
