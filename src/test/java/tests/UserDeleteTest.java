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

public class UserDeleteTest extends BaseTestCase {
    String header;
    String cookie;
    int userIdOnAuth;
    Map<String, String> userData = new HashMap<>();
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void login() {
        // GENERATE USER
        userData = DataGenerator.getRegistrationData();

        Response responseCreateUser = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        // LOGIN
        Response responseGetAuth = apiCoreRequests
                .makePostRequest(url + "user/login", userData);

        this.header = getHeader(responseGetAuth, "x-csrf-token");
        this.cookie = getCookie(responseGetAuth, "auth_sid");
        this.userIdOnAuth = getIntFromJson(responseGetAuth, "user_id");
    }

    @Test
    public void deleteUserWithSpecialIdTest() {
        // LOGIN
        userData.put("email", "vinkotov@example.com");
        userData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(url + "user/login", userData);

        this.header = getHeader(responseGetAuth, "x-csrf-token");
        this.cookie = getCookie(responseGetAuth, "auth_sid");
        this.userIdOnAuth = getIntFromJson(responseGetAuth, "user_id");

        //DELETE
        Response responseDeleteUser = apiCoreRequests
                .makeDeleteRequest(url + "user/" + this.userIdOnAuth,
                        this.header,
                        this.cookie);

        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
        Assertions.assertResponseTextEquals(
                responseDeleteUser,
                "{\"error\":\"Please, do not delete test users with ID 1, 2, 3, 4 or 5.\"}");
    }

    @Test
    public void deleteUserTest() {
        //DELETE
        Response responseDeleteUser = apiCoreRequests.
                makeDeleteRequest(url + "user/" + this.userIdOnAuth,
                        this.header,
                        this.cookie);

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest(
                        url + "user/" + this.userIdOnAuth,
                        this.header,
                        this.cookie);

        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }

    @Test
    public void deleteOtherUserTest() {
        // GENERATE USER
        userData = DataGenerator.getRegistrationData();

        Response responseCreateUser = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        int userIdOnCreate = getIntFromJson(responseCreateUser, "id");

        //DELETE
        Response responseDeleteUser = apiCoreRequests
                .makeDeleteRequest(url + "user/" + this.userIdOnAuth, this.header, this.cookie);

        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
        Assertions.assertResponseTextEquals(
                responseDeleteUser,
                "{\"error\":\"This user can only delete their own account.\"}");
    }
}
