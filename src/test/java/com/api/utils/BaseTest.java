package com.api.utils;

import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.filters(new ResponseLoggingFilter());

        System.out.println("====================================");
        System.out.println("  GoRest API Test Suite - Iniciando");
        System.out.println("====================================");
    }
}
