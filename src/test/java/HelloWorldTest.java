import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest {
    @Test
    public void jsonParsingTest() {
        JsonPath response = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        String answer = response.get("messages[1].message");
        System.out.println(answer);
    }

    @Test
    public void longRedirectTest() {
        Response response = RestAssured
                .given()
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
            Response response = RestAssured
                    .given()
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

        JsonPath response = RestAssured
                .given()
                .get(url)
                .jsonPath();

        String tokenValue = response.get("token");
        int secondsValue = response.get("seconds");

        JsonPath responseBeforeTaskReady = RestAssured
                .given()
                .queryParam("token", tokenValue)
                .get(url)
                .jsonPath();

        if ("Job is NOT ready".equals(responseBeforeTaskReady.get("status"))) {
            Thread.sleep((secondsValue + 1) * 1000L);
            JsonPath responseAfterTaskReady = RestAssured
                    .given()
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
            Response response = RestAssured
                    .given()
                    .queryParam("login", login)
                    .queryParam("password", pass)
                    .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                    .andReturn();

            String responseCookie = response.getCookie("auth_cookie");

            Response checkResponse = RestAssured
                    .given()
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
}

