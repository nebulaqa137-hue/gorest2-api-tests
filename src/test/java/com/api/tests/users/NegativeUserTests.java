package com.api.tests.users;

import com.api.config.RequestSpecFactory;
import com.api.models.User;
import com.api.utils.BaseTest;
import com.api.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Usuarios")
@Feature("Validaciones Negativas")
public class NegativeUserTests extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";
    private static final String USER_BY_ID     = "/users/{id}";

    // ─────────────────────────────────────────────
    // 422 — Datos inválidos / incompletos
    // ─────────────────────────────────────────────

    @Test(description = "POST sin email debe retornar 422")
    @Story("Validación de campos")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_withoutEmail_shouldReturn422() {
        User payload = TestDataFactory.userWithoutEmail();

        given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(422)
                .body("[0].field",   equalTo("email"))
                .body("[0].message", notNullValue());
    }

    @Test(description = "POST sin nombre debe retornar 422")
    @Story("Validación de campos")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_withoutName_shouldReturn422() {
        User payload = TestDataFactory.userWithoutName();

        given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(422)
                .body("[0].field",   equalTo("name"))
                .body("[0].message", notNullValue());
    }

    @Test(description = "POST con gender inválido debe retornar 422")
    @Story("Validación de campos")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_withInvalidGender_shouldReturn422() {
        User payload = TestDataFactory.userWithInvalidGender();

        given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(422)
                .body("[0].field",   equalTo("gender"))
.body("[0].message", containsStringIgnoringCase("male of female"));    }

    @Test(description = "POST con email duplicado debe retornar 422")
    @Story("Email duplicado")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_withDuplicateEmail_shouldReturn422() {
        User original = TestDataFactory.validUser();
        Response firstResponse = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(original)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract().response();

        int originalId = firstResponse.jsonPath().getInt("id");

        try {
            User duplicate = User.builder()
                    .name("Another User")
                    .email(original.getEmail())
                    .gender("male")
                    .status("active")
                    .build();

            given()
                    .spec(RequestSpecFactory.withValidToken())
                    .body(duplicate)
            .when()
                    .post(USERS_ENDPOINT)
            .then()
                    .statusCode(422)
                    .body("[0].field",   equalTo("email"))
                    .body("[0].message", containsStringIgnoringCase("taken"));
        } finally {
            deleteUser(originalId);
        }
    }

    @Test(description = "POST sin body debe retornar 422")
    @Story("Validación de campos")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_withEmptyBody_shouldReturn422() {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .body("{}")
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(422)
                .body("$", not(empty()));
    }

    // ─────────────────────────────────────────────
    // 404 — Recurso no existe
    // ─────────────────────────────────────────────

    @Test(description = "GET usuario con ID inexistente debe retornar 404")
    @Story("Recurso no encontrado")
    @Severity(SeverityLevel.CRITICAL)
    public void getUser_nonExistentId_shouldReturn404() {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", 999999999)
        .when()
                .get(USER_BY_ID)
        .then()
                .statusCode(404)
                .body("message", equalTo("Resource not found"));
    }

    @Test(description = "PUT usuario con ID inexistente debe retornar 404")
    @Story("Recurso no encontrado")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_nonExistentId_shouldReturn404() {
        User payload = TestDataFactory.updatePayload();

        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", 999999999)
                .body(payload)
        .when()
                .put(USER_BY_ID)
        .then()
                .statusCode(404)
                .body("message", equalTo("Resource not found"));
    }

    // ─────────────────────────────────────────────
    // 401 — No autorizado
    // ─────────────────────────────────────────────

    @Test(description = "POST sin token debe retornar 401")
    @Story("No autorizado")
    @Severity(SeverityLevel.BLOCKER)
    public void createUser_withoutToken_shouldReturn401() {
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
    // Helper privado
    // ─────────────────────────────────────────────

    private void deleteUser(int userId) {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", userId)
        .when()
                .delete(USER_BY_ID);
    }
}
