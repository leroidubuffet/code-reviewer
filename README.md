# code-reviewer: ejercicio módulo 2

Microservicio Spring Boot que analiza código con la API de Anthropic.
Integra todos los bloques del módulo: autenticación, Spring AI,
parámetros de llamada, structured output, streaming y reintentos.

---

## Qué tienes que hacer

El ejercicio consiste en implementar los 5 TODOs de `CodeReviewService_B.java`.
Cada TODO corresponde a un bloque del módulo:

| TODO | Qué implementar | Criterio de éxito |
|------|----------------|-------------------|
| TODO 1 | Construir el user message con `<<CODE>>` | El endpoint responde con JSON |
| TODO 2 | Extraer metadata y loguear modelo y tokens | Log `[2]` y `[3]` aparecen en consola |
| TODO 3 | Deserializar la respuesta al record `Review` | El JSON tiene los 4 campos correctos |
| TODO 4 | Distinguir errores 4xx de errores 5xx | `BadRequestException` vs `TransientLlmException` |
| TODO 5 | Implementar el endpoint de streaming | `POST /review/stream` devuelve tokens en tiempo real |

Cuando los cinco criterios sean observables, el ejercicio está completo.

> **El system prompt no es un TODO.**
> Está en `src/main/resources/prompts/system_prompt.txt` y se carga
> automáticamente al arrancar.

---

## Setup

### 1. Requisitos

- JDK 21
- Maven 3.9+
- [`jq`](https://jqlang.org) (para los comandos curl de prueba)
- API key de Anthropic — [console.anthropic.com → API Keys](https://console.anthropic.com)

### 2. Configurar la clave

```bash
export ANTHROPIC_API_KEY=sk-ant-...

# Opcional: modelo más barato para pruebas
export ANTHROPIC_MODEL=claude-haiku-4-5-20251001
```

### 3. Activar la Opción B (punto de partida del ejercicio)

```bash
cp src/main/java/com/curso/reviewer/service/CodeReviewService_B.java \
   src/main/java/com/curso/reviewer/service/CodeReviewService.java
```

### 4. Arrancar

```bash
mvn spring-boot:run
```

> **Opción A — solución de referencia**
> Si quieres ver una implementación completa para comparar:
> ```bash
> cp src/main/java/com/curso/reviewer/service/CodeReviewService_A.java \
>    src/main/java/com/curso/reviewer/service/CodeReviewService.java
> ```

---

## Probar los endpoints

### Endpoint síncrono

```bash
curl -X POST http://localhost:8080/review \
  -H "Content-Type: application/json" \
  -d "$(jq -n --arg lang java --rawfile code inputs/java_simple.txt '{language: $lang, code: $code}')"
```

### Endpoint de streaming (tokens en tiempo real)

```bash
curl --no-buffer -X POST http://localhost:8080/review/stream \
  -H "Content-Type: application/json" \
  -d "$(jq -n --arg lang java --rawfile code inputs/java_simple.txt '{language: $lang, code: $code}')"
```

### Los tres inputs de prueba de una vez

```bash
for f in inputs/java_simple.txt inputs/java_bugs.txt inputs/java_extremo.txt; do
  echo "=== $f ==="
  curl -s -X POST http://localhost:8080/review \
    -H "Content-Type: application/json" \
    -d "$(jq -n --arg lang java --rawfile code "$f" '{language: $lang, code: $code}')" | python3 -m json.tool
  echo
done
```

---

## Criterio de éxito

Los cinco eventos deben ser observables. Los números corresponden a los TODOs:

```
[1+3] La respuesta llega al cliente como JSON con los 4 campos:
      {"score":4,"issues":["..."],"summary":"...","recommendation":"review"}

[2]   INFO  CodeReviewService : LLM call OK — model=claude-sonnet-4-6 ...
[3]   INFO  CodeReviewService : ... input_tokens=487 output_tokens=134 latency_ms=3241

[4]   WARN  CodeReviewService : LLM transient error — status=503 ... — will retry
      WARN  CodeReviewService : LLM transient error — status=503 ... — will retry
      WARN  CodeReviewService : LLM transient error — status=503 ... — will retry

[5]   curl --no-buffer .../review/stream  →  tokens aparecen uno a uno en el terminal
```

---

## Provocar el error 5xx para ver los reintentos (TODO 4)

Edita `application.yml` y añade `base-url` con una URL que no existe:

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      base-url: https://api.anthropic.com/v1/messages-bad   # ← añadir esta línea
```

Arranca, haz una petición y observa los reintentos en el log. Luego elimina
la línea `base-url` para restaurar el comportamiento normal.

Resilience4j reintentará 4 veces con backoff exponencial: 500 ms, 1 s, 2 s.

---

## Calcular el coste del ejercicio

Los logs muestran los tokens consumidos en cada llamada. Para convertirlos
a coste aproximado usa la tarifa pública de Anthropic
([anthropic.com/pricing](https://anthropic.com/pricing), columna claude-sonnet-4-6):

```
coste = (input_tokens_total  / 1_000_000) * precio_input_por_MTok
      + (output_tokens_total / 1_000_000) * precio_output_por_MTok
```
