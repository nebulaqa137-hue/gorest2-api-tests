#  GoRest API Test Suite — REST Assured

Suite de pruebas API con **REST Assured + TestNG** aplicando buenas prácticas de automatización QA.

---

##  Estructura del proyecto

```
gorest-api-tests/
├── src/test/
│   ├── java/com/api/
│   │   ├── config/
│   │   │   ├── ConfigManager.java          ← Singleton para config.properties
│   │   │   └── RequestSpecFactory.java     ← Specs reutilizables (auth, no-auth)
│   │   ├── models/
│   │   │   └── User.java                   ← POJO con Lombok + Jackson
│   │   ├── utils/
│   │   │   ├── BaseTest.java               ← Setup global (BeforeSuite)
│   │   │   └── TestDataFactory.java        ← Generador de datos con Faker
│   │   └── tests/
│   │       ├── auth/
│   │       │   └── AuthTests.java          ← Token válido / inválido / sin token
│   │       ├── users/
│   │       │   ├── UserCrudTests.java      ← POST / GET / PUT / DELETE
│   │       │   └── NegativeUserTests.java  ← 422, 404, 401
│   │       ├── schema/
│   │       │   └── SchemaValidationTests.java ← Validación JSON Schema
│   │       └── integration/
│   │           └── UserIntegrationTests.java  ← Flujo completo (6 pasos)
│   └── resources/
│       ├── config.properties               ← URL, tokens, timeouts
│       ├── testng.xml                      ← Configuración de la suite
│       └── schemas/
│           └── user-schema.json            ← Schema JSON para validación
└── pom.xml
```

---

## ⚙️ Configuración inicial

### 1. Obtener token gratuito

Ir a: [https://gorest.co.in/consumer/login](https://gorest.co.in/consumer/login)

### 2. Pegar token en `config.properties`

```properties
valid.token=TU_TOKEN_REAL_AQUI
```

---

## Ejecutar pruebas

```bash
# Todas las pruebas
mvn clean test

# Solo auth
mvn test -Dtest=AuthTests

# Solo integración
mvn test -Dtest=UserIntegrationTests

# Solo negativos
mvn test -Dtest=NegativeUserTests
```

### Generar reporte Allure

```bash
mvn allure:serve
```

---

## Cobertura de pruebas

| Categoría          | Escenarios | Status Codes         |
|--------------------|-----------|----------------------|
| Auth            | 4         | 200, 401             |
|  CRUD            | 9         | 200, 201, 204, 404   |
|  Negativas        | 8         | 401, 404, 422        |
|  Schema          | 5         | 200, 201             |
|  Integración     | 6         | 200, 201, 204, 404   |
| **TOTAL**          | **~32**   |                      |

---

##  Buenas prácticas implementadas

| Práctica                  | Implementación                                    |
|---------------------------|---------------------------------------------------|
| **Config centralizada**   | `ConfigManager` — Singleton, sin hardcode         |
| **Specs reutilizables**   | `RequestSpecFactory` — auth / sin-auth            |
| **Datos dinámicos**       | `TestDataFactory` + Faker → emails únicos con UUID|
| **Modelos tipados**       | POJO `User` con Lombok + Jackson                  |
| **JSON Schema**           | `matchesJsonSchemaInClasspath()` en cada response |
| **Cleanup**               | Cada test borra los recursos que crea             |
| **Flujo integración**     | `dependsOnMethods` — 6 pasos encadenados          |
| **Reportes**              | Allure con `@Epic` / `@Feature` / `@Story`        |
| **Soft Assertions**       | `SoftAssert` — no abortar en primer fallo         |

---

##  Descripción de módulos

### AuthTests
Valida los tres escenarios de autenticación Bearer Token.

### UserCrudTests
Prueba cada operación CRUD de forma **independiente**. Cada test crea y limpia su propio estado.

### NegativeUserTests
Valida respuestas ante entradas inválidas: campos faltantes, email duplicado, IDs inexistentes.

> GoRest usa **422** para errores de validación de negocio (no 400).

### SchemaValidationTests
Verifica el **contrato de la API** usando `user-schema.json`. Valida tipos, campos requeridos y enums.

### UserIntegrationTests
Flujo completo de 6 pasos encadenados simulando un ciclo de vida real de un usuario.
