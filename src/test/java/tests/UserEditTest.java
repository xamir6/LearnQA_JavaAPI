package tests;

import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserEditTest extends BaseTestCase {
    String cookie;
    String header;
    String userId;
    Map<String, String> userData = new HashMap<>();
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void setUp() {
        // GENERATE USER
        this.userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/", this.userData);

        this.userId = responseCreateAuth.jsonPath().getString("id");

        // LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", this.userData.get("email"));
        authData.put("password", this.userData.get("password"));

        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        this.cookie = getCookie(responseGetAuth, "auth_sid");
        this.header = getHeader(responseGetAuth, "x-csrf-token");
    }

    @Test
    public void editJustCreatedTest() {
        // EDIT
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests
                .makePutRequest(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        this.header,
                        this.cookie,
                        editData
                );

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        this.header,
                        this.cookie
                );

        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    public void editJustCreatedWithoutAuthorisationTest() {
        // EDIT
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", "Changed Name");

        Response responseEditUser = apiCoreRequests
                .makePutRequestWithoutHeaderAndCookie(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        editData
                );

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequestWithoutData("https://playground.learnqa.ru/api/user/" + this.userId);

        String[] notExpectedFields = {"id", "email", "firstName", "lastName"};
        Assertions.assertResponseHasField(responseUserData, "username");
        Assertions.assertJsonHasNotFields(responseUserData, notExpectedFields);
    }

    @Test
    public void editJustCreatedWithOtherUserAuthorisedTest() {
        // EDIT
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", "Changed Name");

        Response responseEditUser = apiCoreRequests
                .makePutRequest(
                        "https://playground.learnqa.ru/api/user/" + 3,
                        this.header,
                        this.cookie,
                        editData
                );

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/" + 3,
                        this.header,
                        this.cookie
                );

        String[] notExpectedFields = {"id", "email", "firstName", "lastName"};
        Assertions.assertResponseHasField(responseUserData, "username");
        Assertions.assertJsonHasNotFields(responseUserData, notExpectedFields);
    }

    @Test
    public void editJustCreatedWithInvalidEmailTest() {
        // EDIT
        Map<String, String> editData = new HashMap<>();
        editData.put("email", "vinkotovexample.com");

        Response responseEditUser = apiCoreRequests
                .makePutRequest(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        this.header,
                        this.cookie,
                        editData
                );

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        this.header,
                        this.cookie
                );

        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
        Assertions.assertResponseTextEquals(responseEditUser, "{\"error\":\"Invalid email format\"}");
    }

    @Test
    public void editJustCreatedWithShortNameTest() {
        // EDIT
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", "o");

        Response responseEditUser = apiCoreRequests
                .makePutRequest(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        this.header,
                        this.cookie,
                        editData
                );

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/" + this.userId,
                        this.header,
                        this.cookie
                );

        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }
}
