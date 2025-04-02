package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lib.ApiCoreRequests;

import java.util.HashMap;
import java.util.Map;

public class UserGetTest extends BaseTestCase {
    String header;
    String cookie;
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void loginUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        this.cookie = getCookie(responseGetAuth, "auth_sid");
        this.header = getHeader(responseGetAuth, "x-csrf-token");

    }

    @Test
    public void getUserDataNotAuthTest() {
        Response responseUserData = RestAssured
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        Assertions.assertResponseHasField(responseUserData, "username");
        Assertions.assertJsonHasNotFields(responseUserData, "firstName");
        Assertions.assertJsonHasNotFields(responseUserData, "lastName");
        Assertions.assertJsonHasNotFields(responseUserData, "email");
    }

    @Test
    public void getUserDetailsAuthAsSameUserTest() {
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/2",
                        this.header,
                        this.cookie
                );

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertResponseHasFields(responseUserData, expectedFields);
    }

    @Test
    public void getUserDetailsOfOtherUserTest() {
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/3",
                        this.header,
                        this.cookie
                );

        Assertions.assertResponseHasField(responseUserData, "username");
        Assertions.assertJsonHasNotFields(responseUserData, "firstName");
        Assertions.assertJsonHasNotFields(responseUserData, "lastName");
        Assertions.assertJsonHasNotFields(responseUserData, "email");
    }
}
