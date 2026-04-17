package com.api.tests.auth;

import com.api.config.RequestSpecFactory;
import com.api.utils.BaseTest;
import com.api.utils.TestDataFactory;
import com.api.models.User;
import io.qameta.allure.*;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * AuthTests — valida los escenarios de autenticación Bearer.
 *
 * ✅ Token válido      → GET /users → 200
 * ❌ Sin token         → POST /users → 401  (GET /users es público en GoRest)
 * ❌ Token inválido    → GET /users → 401, mensaje: "Invalid token"
 * ❌ POST sin token    → POST /users → 401
 */
@Epic("Autenticación")
@Feature("Bearer Token")
public class AuthTests extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";

    // ─────────────────────────────────────────────
    // ✅ Token Válido
    // ─────────────────────────────────────────────

    @Test(description = "GET /users con token válido debe retornar 200")
    @Story("Token Válido")
    @Severity(SeverityLevel.BLOCKER)
    public void validToken_shouldReturn200() {
        given()
                .spec(RequestSpecFactory.withValidToken())
        .when()
                .get(USERS_ENDPOINT)
        .then()
                .statusCode(200)
                .contentType("application/json")
                .body("$", not(empty()));
    }

    // ─────────────────────────────────────────────
    // ❌ Sin Token — usar POST porque GET /users es público en GoRest
    // ─────────────────────────────────────────────

    @Test(description = "POST /users sin token debe retornar 401")
    @Story("Sin Token")
    @Severity(SeverityLevel.CRITICAL)
    public void noToken_shouldReturn401() {
        User payload = TestDataFactory.validUser();

        given()
                .spec(RequestSpecFactory.withoutToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(401)
                .body("message", equalTo("Authentication failed"));
    }

    // ─────────────────────────────────────────────
    // ❌ Token Inválido — GoRest responde "Invalid token"
    // ─────────────────────────────────────────────

    @Test(description = "GET /users con token inválido debe retornar 401")
    @Story("Token Inválido")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidToken_shouldReturn401() {
        given()
                .spec(RequestSpecFactory.withInvalidToken())
        .when()
                .get(USERS_ENDPOINT)
        .then()
                .statusCode(401)
                .body("message", equalTo("Invalid token"));
    }

    // ─────────────────────────────────────────────
    // ❌ POST sin token — validación adicional
    // ─────────────────────────────────────────────

    @Test(description = "POST /users sin token debe retornar 401 (text block)")
    @Story("Sin Token - POST")
    @Severity(SeverityLevel.CRITICAL)
    public void postWithoutToken_shouldReturn401() {
        String body = """
                {
                    "name": "Test User",
                    "email": "test@example.com",
                    "gender": "male",
                    "status": "active"
                }
                """;

        given()
                .spec(RequestSpecFactory.withoutToken())
                .body(body)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(401)
                .body("message", equalTo("Authentication failed"));
    }
}
