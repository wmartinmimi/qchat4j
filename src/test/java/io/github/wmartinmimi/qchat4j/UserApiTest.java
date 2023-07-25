package io.github.wmartinmimi.qchat4j;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class UserApiTest {

    @Test
    public void userListEndpoint() {
        given()
          .when().get("/api/user/list")
          .then()
             .statusCode(200);
    }

}