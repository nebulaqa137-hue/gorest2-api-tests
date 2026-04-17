package com.api.utils;

import com.api.models.User;
import com.github.javafaker.Faker;

import java.util.UUID;

public class TestDataFactory {

    private static final Faker faker = new Faker();

    public static User validUser() {
        return User.builder()
                .name(faker.name().fullName())
                .email(uniqueEmail())
                .gender("male")
                .status("active")
                .build();
    }

    public static User validFemaleUser() {
        return User.builder()
                .name(faker.name().fullName())
                .email(uniqueEmail())
                .gender("female")
                .status("active")
                .build();
    }

    public static User userWithoutEmail() {
        return User.builder()
                .name(faker.name().fullName())
                .gender("male")
                .status("active")
                .build();
    }

    public static User userWithoutName() {
        return User.builder()
                .email(uniqueEmail())
                .gender("female")
                .status("active")
                .build();
    }

    public static User userWithInvalidGender() {
        return User.builder()
                .name(faker.name().fullName())
                .email(uniqueEmail())
                .gender("unknown")
                .status("active")
                .build();
    }

    public static User updatePayload() {
        return User.builder()
                .name("Updated " + faker.name().firstName())
                .status("inactive")
                .build();
    }

    public static String uniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@mailtest.com";
    }
}
