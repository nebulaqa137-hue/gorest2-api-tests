package com.api.utils;
import com.api.models.User;
import com.github.javafaker.Faker;

import java.util.UUID;

/**
 * TestDataFactory — genera datos dinámicos para evitar duplicados en GoRest.
 *
 * GoRest devuelve 422 si el email ya existe → siempre generar email único.
 */
public class TestDataFactory {

    private static final Faker faker = new Faker();

    /**
     * Usuario válido con todos los campos requeridos.
     */
    public static User validUser() {
        return User.builder()
                .name(faker.name().fullName())
                .email(uniqueEmail())
                .gender("male")
                .status("active")
                .build();
    }

    /**
     * Usuario femenino activo.
     */
    public static User validFemaleUser() {
        return User.builder()
                .name(faker.name().fullName())
                .email(uniqueEmail())
                .gender("female")
                .status("active")
                .build();
    }

    /**
     * Usuario sin email — debe producir 422.
     */
    public static User userWithoutEmail() {
        return User.builder()
                .name(faker.name().fullName())
                .gender("male")
                .status("active")
                .build();
    }

    /**
     * Usuario sin nombre — debe producir 422.
     */
    public static User userWithoutName() {
        return User.builder()
                .email(uniqueEmail())
                .gender("female")
                .status("active")
                .build();
    }

    /**
     * Usuario con gender inválido — debe producir 422.
     */
    public static User userWithInvalidGender() {
        return User.builder()
                .name(faker.name().fullName())
                .email(uniqueEmail())
                .gender("unknown")   // valor inválido
                .status("active")
                .build();
    }

    /**
     * Datos de actualización (sin email para evitar conflictos).
     */
    public static User updatePayload() {
        return User.builder()
                .name("Updated " + faker.name().firstName())
                .status("inactive")
                .build();
    }

    /**
     * Email único usando UUID — garantiza sin duplicados.
     */
    public static String uniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@mailtest.com";
    }
}
