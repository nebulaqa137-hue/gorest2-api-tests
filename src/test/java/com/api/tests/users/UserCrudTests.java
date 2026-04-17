package com.api.tests.users;

import com.api.config.RequestSpecFactory;
import com.api.models.User;
import com.api.utils.BaseTest;
import com.api.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Usuarios")
@Feature("CRUD")
public class UserCrudTests extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";
    private static final String USER_BY_ID     = "/users/{id}";

    // ─────────────────────────────────────────────
    // POST — Crear usuario
    // ─────────────────────────────────────────────

    @Test(description = "POST /users con datos válidos debe retornar 201")
    @Story("Crear Usuario")
    @Severity(SeverityLevel.BLOCKER)
    public void createUser_withValidData_shouldReturn201() {
        User payload = TestDataFactory.validUser();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .body("id",     notNullValue())
                .body("name",   equalTo(payload.getName()))
                .body("email",  equalTo(payload.getEmail()))
                .body("gender", equalTo(payload.getGender()))
                .body("status", equalTo(payload.getStatus()))
                .extract().response();

        deleteUser(response.jsonPath().getInt("id"));
    }

    @Test(description = "POST con usuario femenino debe retornar 201")
    @Story("Crear Usuario")
    @Severity(SeverityLevel.NORMAL)
    public void createFemaleUser_shouldReturn201() {
        User payload = TestDataFactory.validFemaleUser();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .body("gender", equalTo("female"))
                .extract().response();

        deleteUser(response.jsonPath().getInt("id"));
    }

    // ─────────────────────────────────────────────
    // GET — Obtener usuario
    // ─────────────────────────────────────────────

    @Test(description = "GET /users debe retornar lista con status 200")
    @Story("Listar Usuarios")
    @Severity(SeverityLevel.CRITICAL)
    public void getUsers_shouldReturn200WithList() {
        given()
                .spec(RequestSpecFactory.withValidToken())
        .when()
                .get(USERS_ENDPOINT)
        .then()
                .statusCode(200)
                .body("$",          not(empty()))
                .body("[0].id",     notNullValue())
                .body("[0].email",  notNullValue());
    }

    @Test(description = "GET /users/{id} con ID existente debe retornar 200")
    @Story("Obtener Usuario por ID")
    @Severity(SeverityLevel.CRITICAL)
    public void getUserById_withValidId_shouldReturn200() {
        int userId = createUserAndGetId();

        try {
            given()
                    .spec(RequestSpecFactory.withValidToken())
                    .pathParam("id", userId)
            .when()
                    .get(USER_BY_ID)
            .then()
                    .statusCode(200)
                    .body("id",     equalTo(userId))
                    .body("name",   notNullValue())
                    .body("email",  notNullValue())
                    .body("gender", notNullValue())
                    .body("status", notNullValue());
        } finally {
            deleteUser(userId);
        }
    }

    @Test(description = "GET /users/{id} con ID inexistente debe retornar 404")
    @Story("Obtener Usuario por ID")
    @Severity(SeverityLevel.CRITICAL)
    public void getUserById_withNonExistentId_shouldReturn404() {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", 9999999999L)
        .when()
                .get(USER_BY_ID)
        .then()
                .statusCode(404)
                .body("message", equalTo("Resource not found"));
    }

    // ─────────────────────────────────────────────
    // PUT — Actualizar usuario
    // ─────────────────────────────────────────────

    @Test(description = "PUT /users/{id} debe actualizar y retornar 200")
    @Story("Actualizar Usuario")
    @Severity(SeverityLevel.CRITICAL)
    public void updateUser_withValidData_shouldReturn200() {
        int userId = createUserAndGetId();
        User updatePayload = TestDataFactory.updatePayload();

        try {
            SoftAssert softAssert = new SoftAssert();

            Response response = given()
                    .spec(RequestSpecFactory.withValidToken())
                    .pathParam("id", userId)
                    .body(updatePayload)
            .when()
                    .put(USER_BY_ID)
            .then()
                    .statusCode(200)
                    .extract().response();

            softAssert.assertEquals(response.jsonPath().getString("name"),   updatePayload.getName(),   "Name no actualizado");
            softAssert.assertEquals(response.jsonPath().getString("status"), updatePayload.getStatus(), "Status no actualizado");
            softAssert.assertEquals(response.jsonPath().getInt("id"),        userId,                    "ID cambió inesperadamente");
            softAssert.assertAll();
        } finally {
            deleteUser(userId);
        }
    }

    @Test(description = "PATCH /users/{id} debe actualizar parcialmente y retornar 200")
    @Story("Actualizar Usuario")
    @Severity(SeverityLevel.NORMAL)
    public void patchUser_shouldReturn200() {
        int userId = createUserAndGetId();
        User partialUpdate = User.builder().name("Patched Name").build();

        try {
            given()
                    .spec(RequestSpecFactory.withValidToken())
                    .pathParam("id", userId)
                    .body(partialUpdate)
            .when()
                    .patch(USER_BY_ID)
            .then()
                    .statusCode(200)
                    .body("name", equalTo("Patched Name"))
                    .body("id",   equalTo(userId));
        } finally {
            deleteUser(userId);
        }
    }

    // ─────────────────────────────────────────────
    // DELETE — Eliminar usuario
    // ─────────────────────────────────────────────

    @Test(description = "DELETE /users/{id} debe retornar 204")
    @Story("Eliminar Usuario")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteUser_withValidId_shouldReturn204() {
        int userId = createUserAndGetId();

        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", userId)
        .when()
                .delete(USER_BY_ID)
        .then()
                .statusCode(204);
    }

    @Test(description = "DELETE /users/{id} con ID inexistente debe retornar 404")
    @Story("Eliminar Usuario")
    @Severity(SeverityLevel.NORMAL)
    public void deleteNonExistentUser_shouldReturn404() {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", 9999999999L)
        .when()
                .delete(USER_BY_ID)
        .then()
                .statusCode(404);
    }

    // ─────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────

    private int createUserAndGetId() {
        User payload = TestDataFactory.validUser();
        return given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getInt("id");
    }

    private void deleteUser(int userId) {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", userId)
        .when()
                .delete(USER_BY_ID);
    }
}
