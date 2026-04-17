package com.api.tests.integration;

import com.api.config.RequestSpecFactory;
import com.api.models.User;
import com.api.utils.BaseTest;
import com.api.utils.TestDataFactory;
import io.qameta.allure.*;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * UserIntegrationTests — flujo completo de integración:
 *
 *   1. POST   → Crear usuario               (201)
 *   2. GET    → Validar usuario creado      (200 + schema)
 *   3. PUT    → Actualizar usuario          (200)
 *   4. GET    → Confirmar actualización     (200)
 *   5. DELETE → Eliminar usuario            (204)
 *   6. GET    → Confirmar eliminación       (404)
 */
@Epic("Integración")
@Feature("Flujo CRUD Completo")
public class UserIntegrationTests extends BaseTest {

    private static final String USERS_ENDPOINT = "/users";
    private static final String USER_BY_ID     = "/users/{id}";
    private static final String USER_SCHEMA    = "schemas/user-schema.json";

    private static int    createdUserId;
    private static String originalEmail;
    private static String originalName;

    @Test(description = "[Paso 1] POST — crear usuario nuevo", groups = {"integration"})
    @Story("Flujo completo")
    @Severity(SeverityLevel.BLOCKER)
    public void step1_createUser() {
        User payload = TestDataFactory.validUser();
        originalEmail = payload.getEmail();
        originalName  = payload.getName();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .body(payload)
        .when()
                .post(USERS_ENDPOINT)
        .then()
                .statusCode(201)
                .body("id",     notNullValue())
                .body("email",  equalTo(originalEmail))
                .body("status", equalTo("active"))
                .extract().response();

        createdUserId = response.jsonPath().getInt("id");
        System.out.println("✅ [Paso 1] Usuario creado con ID: " + createdUserId);
    }

    @Test(description = "[Paso 2] GET — validar usuario creado + schema",
          groups = {"integration"}, dependsOnMethods = "step1_createUser")
    @Story("Flujo completo")
    @Severity(SeverityLevel.CRITICAL)
    public void step2_getAndValidateCreatedUser() {
        SoftAssert soft = new SoftAssert();

        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", createdUserId)
        .when()
                .get(USER_BY_ID)
        .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(USER_SCHEMA))
                .extract().response();

        soft.assertEquals(response.jsonPath().getInt("id"),       createdUserId,  "ID no coincide");
        soft.assertEquals(response.jsonPath().getString("email"),  originalEmail,  "Email no coincide");
        soft.assertEquals(response.jsonPath().getString("name"),   originalName,   "Nombre no coincide");
        soft.assertAll();

        System.out.println("✅ [Paso 2] Usuario validado: " + response.jsonPath().getString("email"));
    }

    @Test(description = "[Paso 3] PUT — actualizar nombre y status del usuario",
          groups = {"integration"}, dependsOnMethods = "step2_getAndValidateCreatedUser")
    @Story("Flujo completo")
    @Severity(SeverityLevel.CRITICAL)
    public void step3_updateUser() {
        User updatePayload = TestDataFactory.updatePayload();

        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", createdUserId)
                .body(updatePayload)
        .when()
                .put(USER_BY_ID)
        .then()
                .statusCode(200)
                .body("id",     equalTo(createdUserId))
                .body("name",   equalTo(updatePayload.getName()))
                .body("status", equalTo("inactive"));

        System.out.println("✅ [Paso 3] Usuario actualizado a: " + updatePayload.getName());
    }

    @Test(description = "[Paso 4] GET — confirmar que la actualización persiste",
          groups = {"integration"}, dependsOnMethods = "step3_updateUser")
    @Story("Flujo completo")
    @Severity(SeverityLevel.NORMAL)
    public void step4_confirmUpdate() {
        Response response = given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", createdUserId)
        .when()
                .get(USER_BY_ID)
        .then()
                .statusCode(200)
                .body("status", equalTo("inactive"))
                .body("email",  equalTo(originalEmail))
                .extract().response();

        System.out.println("✅ [Paso 4] Actualización confirmada. Status: " + response.jsonPath().getString("status"));
    }

    @Test(description = "[Paso 5] DELETE — eliminar el usuario creado",
          groups = {"integration"}, dependsOnMethods = "step4_confirmUpdate")
    @Story("Flujo completo")
    @Severity(SeverityLevel.CRITICAL)
    public void step5_deleteUser() {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", createdUserId)
        .when()
                .delete(USER_BY_ID)
        .then()
                .statusCode(204);

        System.out.println("✅ [Paso 5] Usuario eliminado con ID: " + createdUserId);
    }

    @Test(description = "[Paso 6] GET — confirmar que el usuario ya no existe (404)",
          groups = {"integration"}, dependsOnMethods = "step5_deleteUser")
    @Story("Flujo completo")
    @Severity(SeverityLevel.CRITICAL)
    public void step6_confirmDeletion() {
        given()
                .spec(RequestSpecFactory.withValidToken())
                .pathParam("id", createdUserId)
        .when()
                .get(USER_BY_ID)
        .then()
                .statusCode(404)
                .body("message", equalTo("Resource not found"));

        System.out.println("✅ [Paso 6] Eliminación confirmada. ID " + createdUserId + " ya no existe.");
        System.out.println("🎉 Flujo de integración completo exitosamente.");
    }
}
