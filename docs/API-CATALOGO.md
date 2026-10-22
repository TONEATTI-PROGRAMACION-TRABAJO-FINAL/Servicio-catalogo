# API de consulta del catálogo

Contrato entre **Servicio-catalogo** (proveedor) y **Servicio-turnos** (único consumidor). Según la sección 19 de la referencia de integración de la cátedra, los endpoints y DTO entre los servicios propios los define cada alumno: este documento es esa definición.

## Para qué la usa Servicio-turnos

En el flujo de reserva (sección 17 de la referencia), turnos le pide a catálogo la agenda vigente de un profesional para calcular los slots disponibles, antes de restarle las ocupaciones que informa la cátedra:

1. Para que la app busque: categorías y profesionales (`GET /professional-categories`, `GET /professionals?categoryId=`).
2. Para validar la selección y mostrar el nombre en sus reservas: un profesional por id (`GET /professionals/{id}`).
3. Para generar los slots: los horarios semanales del profesional (`GET /weekly-schedules?professionalId=`).

## Convenciones

- URL base: la del servicio, puerto por defecto `8080`. Rutas sin prefijo.
- Solo lectura: todos los endpoints son `GET`.
- JSON con propiedades en `camelCase`.
- Horas locales de atención en formato `HH:mm:ss`.
- `dayOfWeek` admite `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY` y `SUNDAY`, los mismos valores que la cátedra.
- Los ids son los que asigna la cátedra en la sincronización, no ids locales.
- Las respuestas no incluyen `createdAt` ni `updatedAt`: son metadatos de la sincronización con la cátedra y turnos no los necesita.
- Documentación interactiva (springdoc): `/swagger-ui.html` y la especificación OpenAPI en `/v3/api-docs`.

## Entidades deshabilitadas

El catálogo guarda una copia local que incluye entidades habilitadas y deshabilitadas, tal como las entrega la cátedra.

- **Los listados devuelven solo las habilitadas.** Un profesional, una categoría o un horario deshabilitado no puede ofrecerse para reservar.
- **`GET /professionals/{id}` devuelve el profesional aunque esté deshabilitado**, con el campo `enabled`. Turnos puede tener reservas anteriores de un profesional que después se deshabilitó y necesita mostrar su nombre; con el flag decide si lo ofrece o no.

## Endpoints

### `GET /professional-categories`

Lista las categorías habilitadas.

Respuesta `200 OK`:

```json
[
  {
    "id": 1,
    "name": "Clinica medica",
    "description": "Atencion clinica general"
  }
]
```

Si no hay categorías habilitadas, devuelve `200 OK` con `[]`.

### `GET /professionals`

Lista los profesionales habilitados. Con `categoryId`, solo los de esa categoría.

| Parámetro | Ubicación | Tipo | Obligatorio |
| --- | --- | --- | --- |
| `categoryId` | query | entero | no |

Respuesta `200 OK`:

```json
[
  {
    "id": 15,
    "categoryId": 1,
    "firstName": "Ana",
    "lastName": "Gomez",
    "enabled": true
  }
]
```

Una categoría sin profesionales habilitados, o inexistente, devuelve `200 OK` con `[]`. Catálogo no distingue esos dos casos: el filtro sin resultados es una respuesta válida.

### `GET /professionals/{id}`

Devuelve un profesional, habilitado o no.

| Parámetro | Ubicación | Tipo | Obligatorio |
| --- | --- | --- | --- |
| `id` | path | entero | sí |

Respuesta `200 OK`:

```json
{
  "id": 15,
  "categoryId": 1,
  "firstName": "Ana",
  "lastName": "Gomez",
  "enabled": false
}
```

Si el id no existe, `404 Not Found` (ver [Errores](#errores)).

### `GET /weekly-schedules`

Lista los horarios semanales habilitados de un profesional.

| Parámetro | Ubicación | Tipo | Obligatorio |
| --- | --- | --- | --- |
| `professionalId` | query | entero | sí |

Respuesta `200 OK`:

```json
[
  {
    "id": 21,
    "professionalId": 15,
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "12:00:00",
    "slotDurationMinutes": 30
  }
]
```

Un profesional sin horarios habilitados, o inexistente, devuelve `200 OK` con `[]`. Para saber si el profesional existe o está habilitado, turnos consulta `GET /professionals/{id}`.

## Errores

Los errores usan `application/problem+json` (RFC 9457), el mismo formato que la API de la cátedra. A diferencia de la cátedra, no incluyen un campo `code`: el `status` alcanza para los casos de esta API.

| Situación | Status |
| --- | --- |
| `GET /professionals/{id}` con un id inexistente | `404 Not Found` |
| `id`, `categoryId` o `professionalId` no numérico | `400 Bad Request` |
| `GET /weekly-schedules` sin `professionalId` | `400 Bad Request` |

Ejemplos:

```http
GET /professionals/99

HTTP/1.1 404
Content-Type: application/problem+json

{"instance":"/professionals/99","status":404,"title":"Not Found"}
```

```http
GET /weekly-schedules

HTTP/1.1 400
Content-Type: application/problem+json

{"detail":"Required parameter 'professionalId' is not present.","instance":"/weekly-schedules","status":400,"title":"Bad Request"}
```

## Seguridad

Los endpoints **no requieren autenticación**.

- Catálogo no valida el JWT del usuario final: la app KMP habla solo con turnos, nunca con catálogo.
- Son endpoints de solo lectura sobre datos no sensibles: el mismo catálogo que la app le muestra a cualquier usuario.
- La protección es de red: el puerto de catálogo no se publica y solo turnos lo alcanza.
- El JWT técnico de la cátedra no se usa entre los servicios propios: identifica al proyecto ante la cátedra y no debe circular por otros canales.

La autenticación entre servicios con una API key queda para un issue aparte.
