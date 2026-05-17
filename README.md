# code-reviewer: ejercicio integrador · módulo 2

Microservicio Spring Boot que analiza código con la API de Anthropic.
Integra todos los bloques del módulo: autenticación, HttpClient (abstraído),
Spring AI, parámetros de llamada, structured output, streaming y reintentos.

---

## Cambiar entre opción A y opción B

El proyecto tiene dos versiones del servicio principal. Solo una puede estar
activa a la vez. Para cambiar:

**Activar opción A (código completo):**
```bash
cp src/main/java/com/curso/reviewer/service/CodeReviewService_A.java \
   src/main/java/com/curso/reviewer/service/CodeReviewService.java
```

**Activar opción B (TODOs para el alumno):**
```bash
cp src/main/java/com/curso/reviewer/service/CodeReviewService_B.java \
   src/main/java/com/curso/reviewer/service/CodeReviewService.java
```

El resto de ficheros (pom.xml, application.yml, ReviewController, Review,
ReviewRequest, ChatClientConfig) son idénticos en las dos opciones.

---

## Setup

### 1. Requisitos
- JDK 21
- Maven 3.9+
- API key de Anthropic (console.anthropic.com → API Keys)

### 2. Configurar la clave
```bash
export ANTHROPIC_API_KEY=sk-ant-...

# Opcional: cambiar el modelo sin tocar código
export ANTHROPIC_MODEL=claude-haiku-4-5-20251001  # más barato para pruebas
```

### 3. Activar una opción y arrancar
```bash
# Opción A (código completo)
cp src/main/java/com/curso/reviewer/service/CodeReviewService_A.java \
   src/main/java/com/curso/reviewer/service/CodeReviewService.java

mvn spring-boot:run
```

---

## Probar los endpoints

### Endpoint síncrono
```bash
curl -X POST http://localhost:8080/review \
  -H "Content-Type: application/json" \
  -d "{\"language\": \"java\", \"code\": \"$(cat inputs/java_simple.txt)\"}"
```

### Endpoint de streaming (ver tokens llegar en tiempo real)
```bash
curl --no-buffer -X POST http://localhost:8080/review/stream \
  -H "Content-Type: application/json" \
  -d "{\"language\": \"java\", \"code\": \"$(cat inputs/java_simple.txt)\"}"
```

### Con los tres inputs de prueba
```bash
for f in inputs/java_simple.txt inputs/java_bugs.txt inputs/java_extremo.txt; do
  echo "=== $f ==="
  curl -s -X POST http://localhost:8080/review \
    -H "Content-Type: application/json" \
    -d "{\"language\": \"java\", \"code\": \"$(cat $f)\"}" | python3 -m json.tool
  echo
done
```

---

## Criterio de éxito

Los cinco eventos deben ser observables en los logs:

```
[1] DEBUG ReviewController  : POST /review — language=java, codeLength=312
[2] INFO  CodeReviewService : LLM call OK — model=claude-sonnet-4-6 ...
[3] INFO  CodeReviewService : ... input_tokens=487 output_tokens=134 latency_ms=3241
[4] WARN  CodeReviewService : LLM transient error — status=503 ... — will retry
    WARN  CodeReviewService : LLM transient error — status=503 ... — will retry
    WARN  CodeReviewService : LLM transient error — status=503 ... — will retry
[5] La respuesta llega al cliente como JSON tipado:
    {"score":4,"issues":["...","..."],"summary":"..."}
```

---

## Provocar el error 5xx para ver los reintentos (criterio 4)

**Opción rápida — URL incorrecta temporal:**

En `application.yml`, cambiar temporalmente:
```yaml
spring:
  ai:
    anthropic:
      base-url: https://api.anthropic.com/v1/messages-bad  # URL que no existe
```
Arrancar, hacer una petición, observar los reintentos en el log, restaurar la URL.

**Opción con red — desactivar la red de la MV:**

Desde el terminal de la MV mientras la aplicación está corriendo:
```bash
# Desactivar red (requiere sudo)
sudo ip link set eth0 down

# Hacer la petición en otro terminal — verás los reintentos
curl -X POST http://localhost:8080/review \
  -H "Content-Type: application/json" \
  -d '{"language": "java", "code": "public void test() {}"}'

# Restaurar red
sudo ip link set eth0 up
```

Resilience4j reintentará 4 veces con backoff exponencial: 500ms, 1s, 2s.
Los tres reintentos son visibles en el log con timestamp.

---

## Calcular el coste del ejercicio

Al terminar, los logs tienen el total de tokens consumidos. Para convertirlos
a coste aproximado, usar la tarifa pública de Anthropic
(anthropic.com/pricing, columna claude-sonnet-4-6):

```
coste = (input_tokens_total  / 1_000_000) * precio_input_por_MTok
      + (output_tokens_total / 1_000_000) * precio_output_por_MTok
```

Ejemplo con los valores del log:
```
input_tokens  = 1_847   →  1.847 / 1000 * precio_input
output_tokens =   412   →  0.412 / 1000 * precio_output
```

El resultado suele ser menos de 0,01 €. Convierte los tokens de algo abstracto
en algo concreto.
