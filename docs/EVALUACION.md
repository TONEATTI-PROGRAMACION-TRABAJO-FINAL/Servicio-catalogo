<!-- evaluacion:inicio -->
# Evaluación — TONEATTI-PROGRAMACION-TRABAJO-FINAL/Servicio-catalogo

> Documento **generado automáticamente** por la cátedra de Programación II (UM): RepoSense + análisis de calidad de **7 dimensiones**. **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas) + build: es una criba para orientar la revisión, **no una nota**. La **documentación** del repositorio no se evalúa.
> Generado: 2026-10-17.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad | **56/100** |
| Tipo de repositorio | `backend` |
| Build (`mvnw verify` / `gradlew check`) | OK |
| Código (LOC) | 845 |
| Archivos de código | 26 |
| Clases | 26 |
| Código dentro de clases (%POO) | 72% |
| Funciones fuera de clases | 0 |
| Duplicación | 39.9% |
| Cobertura | 98% |
| Tope duro aplicado a D1 | `DUP>=30%` → D1 ≤ 30 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

## 2. Semáforo de aprobación

Criterios de la cátedra (`CRITERIOS-CALIDAD.md`): **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **no cumple** — 7/11 |
| Destacar (`exigente`) | **no cumple** — 5/11 |

**Para aprobar, falta cumplir:** Duplicación

**Para destacar, falta cumplir:** SOLID; POO; Duplicación

## 3. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| LucianoToneatti | 16 | 432 | 553 | 1 | 5 | 100% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis (que mira el estado de `main`)._

## 4. Dimensiones evaluadas

Cada dimensión indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el puntaje queda por debajo de la referencia, se agregan preguntas para **investigar la causa** (no hay recetas).

| Dimensión | Peso | Puntaje |
|:--|--:|--:|
| **D1** Diseño OO, SOLID y patrones (20%) | 20% | 9 |
| **D2** Arquitectura hexagonal — backend (15%) | 15% | 64 |
| **D3** Frontend KMP: MVVM y buenas prácticas (10%) | 10% | N/A |
| **D4** Integración y contrato (10%) | 10% | 0 |
| **D5** Seguridad y robustez (10%) | 10% | 95 |
| **D6** Pruebas y cobertura (20%) | 20% | 100 |
| **D7** Proceso y entrega (15%) | 15% | 62 |

### D1. Diseño OO, SOLID y patrones (20%)

- **Qué mide:** Métricas orientadas a objetos (clases, métodos, campos, cohesión, acoplamiento, herencia) y proxies de SOLID, más patrones de diseño.
- **Por qué importa:** El eje de la materia es el buen uso de la POO y su arquitectura: un diseño con clases claras y poco acopladas es más fácil de entender, probar y cambiar.
- **Tu resultado:** **9/100**
- **Para investigar la causa:**
    - ¿Se pensó el modelo de clases antes de codificar o se agrupó después?
    - ¿Qué clases concentran muchas responsabilidades a la vez?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### D2. Arquitectura hexagonal — backend (15%)

- **Qué mide:** Separación en capas (dominio, puertos de entrada/salida, adaptadores) y dirección de las dependencias del backend.
- **Por qué importa:** Aislar el dominio de la infraestructura permite cambiar la base de datos, el framework o el transporte sin tocar las reglas de negocio.
- **Tu resultado:** **64/100**

### D4. Integración y contrato (10%)

- **Qué mide:** Cumplimiento del contrato de integración de la cátedra (formato de errores, paginación, autenticación, endpoints esperados).
- **Por qué importa:** Los servicios deben poder integrarse entre sí y con la app sin sorpresas; el contrato es lo que hacen cumplir las pruebas de integración.
- **Tu resultado:** **0/100**
- **Para investigar la causa:**
    - ¿Qué endpoints difieren del contrato acordado?
    - ¿Los errores se devuelven con un formato uniforme?
    - ¿La autenticación es consistente entre servicios?

### D5. Seguridad y robustez (10%)

- **Qué mide:** Credenciales embebidas, consultas SQL por concatenación, y presencia de config de entorno; aislamiento de secretos.
- **Por qué importa:** Un secreto en el repositorio o una consulta construida por concatenación son riesgos concretos, no hipotéticos.
- **Tu resultado:** **95/100**

### D6. Pruebas y cobertura (20%)

- **Qué mide:** Cantidad y resultado de pruebas y cobertura de línea (JaCoCo/Kover, Surefire/Failsafe) cuando hay build.
- **Por qué importa:** Las pruebas son la red de seguridad para refactorizar e integrar; la cobertura acota las zonas sin verificar.
- **Tu resultado:** **100/100**

### D7. Proceso y entrega (15%)

- **Qué mide:** Artefactos verificables de proceso/entrega: `compose`, `.env.example`, CI (`.github/workflows/`), `CODEOWNERS` y wrapper de build (`mvnw`/`gradlew`). **No** puntúa documentación.
- **Por qué importa:** Un proyecto que compila, se levanta y se valida solo es más fácil de revisar y de integrar en equipo.
- **Tu resultado:** **62/100**

## 5. Señales detectadas

El análisis encontró las siguientes señales (archivo, sin número de línea a propósito):

**SRP/responsabilidad** (8)
- clase 'ProfessionalEntity' sin métodos (7 campos): solo datos — `src/main/java/com/toneatti/catalogo/professional/infrastructure/persistence/entity/ProfessionalEntity.java`
- clase 'Professional' sin métodos (7 campos): solo datos — `src/main/java/com/toneatti/catalogo/professional/domain/model/Professional.java`
- clase 'ProfessionalCategoryEntity' sin métodos (6 campos): solo datos — `src/main/java/com/toneatti/catalogo/professionalcategory/infrastructure/persistence/entity/ProfessionalCategoryEntity.java`
- clase 'ProfessionalCategory' sin métodos (6 campos): solo datos — `src/main/java/com/toneatti/catalogo/professionalcategory/domain/model/ProfessionalCategory.java`
- clase 'WeeklyScheduleEntity' grande (LOC=13, métodos=0, campos=9) — `src/main/java/com/toneatti/catalogo/weeklyschedule/infrastructure/persistence/entity/WeeklyScheduleEntity.java`
- clase 'WeeklyScheduleEntity' sin métodos (9 campos): solo datos — `src/main/java/com/toneatti/catalogo/weeklyschedule/infrastructure/persistence/entity/WeeklyScheduleEntity.java`
- clase 'WeeklySchedule' grande (LOC=11, métodos=0, campos=9) — `src/main/java/com/toneatti/catalogo/weeklyschedule/domain/model/WeeklySchedule.java`
- clase 'WeeklySchedule' sin métodos (9 campos): solo datos — `src/main/java/com/toneatti/catalogo/weeklyschedule/domain/model/WeeklySchedule.java`

**DIP/concretos** (6)
- clase 'WeeklyScheduleEntity' muy acoplada (CBO=11) — `src/main/java/com/toneatti/catalogo/weeklyschedule/infrastructure/persistence/entity/WeeklyScheduleEntity.java`
- clase 'ProfessionalMapperTest' muy acoplada (CBO=8) — `src/test/java/com/toneatti/catalogo/professional/infrastructure/persistence/mapper/ProfessionalMapperTest.java`
- clase 'JpaProfessionalRepositoryAdapterTest' muy acoplada (CBO=10) — `src/test/java/com/toneatti/catalogo/professional/infrastructure/persistence/adapter/JpaProfessionalRepositoryAdapterTest.java`
- clase 'JpaProfessionalCategoryRepositoryAdapterTest' muy acoplada (CBO=10) — `src/test/java/com/toneatti/catalogo/professionalcategory/infrastructure/persistence/adapter/JpaProfessionalCategoryRepositoryAdapterTest.java`
- clase 'WeeklyScheduleMapperTest' muy acoplada (CBO=8) — `src/test/java/com/toneatti/catalogo/weeklyschedule/infrastructure/persistence/mapper/WeeklyScheduleMapperTest.java`
- clase 'JpaWeeklyScheduleRepositoryAdapterTest' muy acoplada (CBO=11) — `src/test/java/com/toneatti/catalogo/weeklyschedule/infrastructure/persistence/adapter/JpaWeeklyScheduleRepositoryAdapterTest.java`

**duplicación** (1)
- duplicación de bloques: 39.9% del código

**hexagonal** (1)
- falta ports/in (hexagonal)

## 6. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (3 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice de calidad a lo largo de los commits"
    x-axis ["47087b2", "13c8139", "646e875"]
    y-axis "Índice" 0 --> 100
    line [0, 15, 38]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-09-10 | `47087b2` | LucianoToneatti | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-09-15 | `13c8139` | Ing. Daniel Quinteros | 0 | 15 | 0% | 0.0% | 0 |
| 3 | 2026-10-13 | `646e875` | LucianoToneatti | 845 | 38 | 72% | 39.9% | 0 |

---
_Informe generado por la cátedra. Las dimensiones y umbrales corresponden a `calidad.py` (perfil `exigente`); el semáforo, a `CRITERIOS-CALIDAD.md`. La documentación del repositorio no se evalúa. Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
