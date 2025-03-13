import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class HelloWorldTest {
    @Test
    public void JsonParsingTest() {
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
                .follow(true)
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        Headers responceHeaders = response.getHeaders();
        Header siteHeader = responceHeaders.get("x-host");
        String value = siteHeader.getValue();
        System.out.println(value);
    }
}
