package tests;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest {
    @Test
    public void jsonParsingTest() {
        JsonPath response = given()
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        String answer = response.get("messages[1].message");
        System.out.println(answer);
    }

    @Test
    public void longRedirectTest() {
        Response response = given()
                .redirects()
                .follow(false)
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        Headers responseHeaders = response.getHeaders();
        Header siteHeader = responseHeaders.get("Location");
        String value = siteHeader.getValue();
        System.out.println(value);
    }

    @Test
    public void longAllRedirectsTest() {
        String currentUrl = "https://playground.learnqa.ru/api/long_redirect";

        while (true) {
            Response response = given()
                    .redirects()
                    .follow(false)
                    .get(currentUrl)
                    .andReturn();

            if (response.getStatusCode() != 200) {
                currentUrl = response.getHeader("Location");
                System.out.println(currentUrl);
            } else
                break;
        }
    }

    @Test
    public void longtimeJobTest() throws InterruptedException {
        String url = "https://playground.learnqa.ru/ajax/api/longtime_job";

        JsonPath response = given()
                .get(url)
                .jsonPath();

        String tokenValue = response.get("token");
        int secondsValue = response.get("seconds");

        JsonPath responseBeforeTaskReady = given()
                .queryParam("token", tokenValue)
                .get(url)
                .jsonPath();

        if ("Job is NOT ready".equals(responseBeforeTaskReady.get("status"))) {
            Thread.sleep((secondsValue + 1) * 1000L);
            JsonPath responseAfterTaskReady = given()
                    .queryParam("token", tokenValue)
                    .get(url)
                    .jsonPath();

            if ("Job is ready".equals(responseAfterTaskReady.get("status")) && (responseAfterTaskReady.get("result") != null)) {
                System.out.println("We got it");
            }
        }
    }

    @Test
    public void getSecretPasswordTest() {
        String login = "super_admin";
        List<String> passwords = Arrays.asList(
                "12345", "123456789", "12345678", "qwerty", "abc123", "12345", "1234567", "letmein", "dragon", "monkey", "111111",
                "baseball", "iloveyou", "sunshine", "trustno1", "master", "ashley", "bailey", "passw0rd", "shadow", "654321",
                "jesus", "password1", "football", "000000", "sunshine", "solo", "adobe123", "adobe", "admin", "princess",
                "photoshop", "mustang", "starwars", "654321", "1q2w3e4r", "qwerty123", "555555", "ashley", "lovely", "!@#$%^&*",
                "welcome", "qazwsx", "zaq1zaq1", "michael", "hottie", "freedom", "donald", "access"
        );

        for (String pass : passwords) {
            Response response = given()
                    .queryParam("login", login)
                    .queryParam("password", pass)
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String responseCookie = response.getCookie("auth_cookie");

            Response checkResponse = given()
                    .cookie("auth_cookie", responseCookie)
                    .get("https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                    .andReturn();

            String resultCheck = checkResponse.asString();

            if (!resultCheck.contains("You are NOT authorized")) {
                System.out.println("Correct pass " + pass);
                System.out.println("Response " + resultCheck);
            }
        }
    }

    @Test
    public void checkTextLengthTest() {
        String hello = "Hello, world";
        int expectedTextMinLength = 15;

        if (hello.length() > 15) {
            assertEquals(hello.length(), hello.length());
        } else {
            assertEquals(expectedTextMinLength + 1, hello.length(), "Text length less that 15 letters.");
        }
    }

    @Test
    public void responseCookieKeyAndValueTest() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_cookie")
                .andReturn();

        String responseKeyCookie = response.getCookies().keySet().iterator().next();
        String responseValueCookie = response.getCookie(responseKeyCookie);

        assertEquals("HomeWork", responseKeyCookie, "The cookie key is incorrect");
        assertEquals("hw_value", responseValueCookie, "The cookie value is incorrect");
    }

    @Test
    public void responseHeaderTest() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/homework_header")
                .andReturn();

        String header = response.getHeader("x-secret-homework-header");
        assertEquals("Some secret value", header);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
            "Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0",
            "Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
    })

    public void userAgentTest(String userAgent) {
        Response response = given()
                .header("User-Agent", userAgent) // Передаем User-Agent как заголовок
                .get("https://playground.learnqa.ru/ajax/api/user_agent_check");
        response.prettyPrint();

        String actualPlatform = response.jsonPath().getString("platform");
        String actualBrowser = response.jsonPath().getString("browser");
        String actualDevice = response.jsonPath().getString("device");

        switch (userAgent) {
            case "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30":
                assertEquals("Mobile", actualPlatform);
                assertEquals("No", actualBrowser);
                assertEquals("Android", actualDevice);
                break;

            case "Mozilla/5.0 (iPad; CPU OS 13_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/91.0.4472.77 Mobile/15E148 Safari/604.1":
                assertEquals("Mobile", actualPlatform);
                assertEquals("Chrome", actualBrowser);
                assertEquals("iOS", actualDevice);
                break;

            case "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)":
                assertEquals("Googlebot", actualPlatform);
                assertEquals("Unknown", actualBrowser);
                assertEquals("Unknown", actualDevice);
                break;

            case "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.100.0":
                assertEquals("Web", actualPlatform);
                assertEquals("Chrome", actualBrowser);
                assertEquals("No", actualDevice);
                break;

            case "Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1":
                assertEquals("Mobile", actualPlatform);
                assertEquals("No", actualBrowser);
                assertEquals("iPhone", actualDevice);
                break;
        }
    }
}
