package com.api.tests.schema;

import com.api.config.RequestSpecFactory;
import com.api.models.User;
import com.api.utils.BaseTest;
import com.api.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Validación de Contrato")
@Feature("JSON Schema")
public class SchemaValidationTests extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";
    private static final String USER_BY_ID     = "/users/{id}";
    private static final String USER_SCHEMA    = "schemas/user-schema.json";

    @Test(description = "POST /users — response debe cumplir user-schema.json")
    @Story("Schema de creación")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_responseShouldMatchSchema() {
        User payload = TestDataFactory.validUser();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(USER_SCHEMA))
                .extract().response();

        deleteUser(response.jsonPath().getInt("id"));
    }

    @Test(description = "GET /users/{id} — response debe cumplir user-schema.json")
    @Story("Schema de consulta")
    @Severity(SeverityLevel.CRITICAL)
    public void getUser_responseShouldMatchSchema() {
        int userId = createUserAndGetId();

        try {
            given()
                    .spec(RequestSpecFactory.withValidToken())
                    .pathParam("id", userId)
            .when()
                    .get(USER_BY_ID)
            .then()
                    .statusCode(200)
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(USER_SCHEMA));
        } finally {
            deleteUser(userId);
        }
    }

    @Test(description = "Response debe tener tipos de datos correctos (id=int, name=string)")
    @Story("Tipos de datos")
    @Severity(SeverityLevel.NORMAL)
    public void createUser_shouldHaveCorrectDataTypes() {
        User payload = TestDataFactory.validUser();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .extract().response();

        int userId = response.jsonPath().getInt("id");

        try {
            assertThat("id debe ser Integer",   response.jsonPath().get("id"),     instanceOf(Integer.class));
            assertThat("name debe ser String",  response.jsonPath().get("name"),   instanceOf(String.class));
            assertThat("email debe ser String", response.jsonPath().get("email"),  instanceOf(String.class));
            assertThat("gender es válido",      response.jsonPath().getString("gender"), anyOf(equalTo("male"), equalTo("female")));
            assertThat("status es válido",      response.jsonPath().getString("status"), anyOf(equalTo("active"), equalTo("inactive")));
        } finally {
            deleteUser(userId);
        }
    }

    @Test(description = "Response POST debe contener todos los campos requeridos")
    @Story("Campos requeridos")
    @Severity(SeverityLevel.CRITICAL)
    public void createUser_shouldContainAllRequiredFields() {
        User payload = TestDataFactory.validUser();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .body("id",     notNullValue())
                .body("name",   notNullValue())
                .body("email",  notNullValue())
                .body("gender", notNullValue())
                .body("status", notNullValue())
                .extract().response();

        deleteUser(response.jsonPath().getInt("id"));
    }

    @Test(description = "PUT /users/{id} — response debe cumplir user-schema.json")
    @Story("Schema de actualización")
    @Severity(SeverityLevel.NORMAL)
    public void updateUser_responseShouldMatchSchema() {
        int userId = createUserAndGetId();

        try {
            User updatePayload = TestDataFactory.updatePayload();

            given()
                    .spec(RequestSpecFactory.withValidToken())
                    .pathParam("id", userId)
                    .body(updatePayload)
            .when()
                    .put(USER_BY_ID)
            .then()
                    .statusCode(200)
                    .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(USER_SCHEMA));
        } finally {
            deleteUser(userId);
        }
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
