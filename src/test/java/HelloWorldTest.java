import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

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
}


