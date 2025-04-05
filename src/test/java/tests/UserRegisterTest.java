package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.Assertions;
import lib.ApiCoreRequests;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Tag("Some service name")
public class UserRegisterTest extends BaseTestCase {
    Map<String, String> userData = new HashMap<>();
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    public void createUserWithExistingEmailTest() {
        String email = "vinkotov@example.com";
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    public void createUserWithIncorrectEmail() {
        String incorrectEmail = "vinkotovexample.com";
        userData.put("email", incorrectEmail);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "password", "username", "firstName", "lastName"})
    public void createUserWithoutOneField(String condition) {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.put(condition, null);

        RequestSpecification spec = RestAssured.given();
        spec.baseUri(url + "user/");

        Response responseForCheck = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        Assertions.assertResponseCodeEquals(responseForCheck, 400);
        Assertions.assertResponseTextEquals(responseForCheck, "The following required params are missed: " + condition);
    }

    @Test
    public void createUserWithShortName() {
        String shortUsername = "o";
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.put("username", shortUsername);

        Response responseForCheck = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        Assertions.assertResponseCodeEquals(responseForCheck, 400);
        Assertions.assertResponseTextEquals(responseForCheck, "The value of 'username' field is too short");
    }

    @Test
    public void createLongUserName() {
        String longUsername = DataGenerator.getLongUsername();
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.put("username", longUsername);

        Response responseForCheck = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        Assertions.assertResponseCodeEquals(responseForCheck, 400);
        Assertions.assertResponseTextEquals(responseForCheck, "The value of 'username' field is too long");
    }

    @Test
    public void createUserSuccessfully() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(url + "user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertResponseHasField(responseCreateAuth, "id");
    }
}
