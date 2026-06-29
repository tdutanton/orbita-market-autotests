package orbitaMarket.service;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;

public class PaymentsApi {

  private static final String BASE_PATH = "/payments";

  public Response createAccount(String userId) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .when()
        .post(BASE_PATH + "/accounts")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response createAccountWithoutUserId() {
    return given()
        .contentType(ContentType.JSON)
        .when()
        .post(BASE_PATH + "/accounts")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response topUp(String userId, BigDecimal value) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .body(Map.of("value", value))
        .when()
        .post(BASE_PATH + "/accounts/top-up")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response topUpWithInvalidBody(String userId, Object body) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .body(body)
        .when()
        .post(BASE_PATH + "/accounts/top-up")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response topUpWithoutUserId(int amount) {
    return given()
        .contentType(ContentType.JSON)
        .body(Map.of("amount", amount))
        .when()
        .post(BASE_PATH + "/accounts/top-up")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response getBalance(String userId) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .when()
        .get(BASE_PATH + "/accounts/balance")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response getBalanceWithoutUserId() {
    return given()
        .contentType(ContentType.JSON)
        .when()
        .get(BASE_PATH + "/accounts/balance")
        .then()
        .log().body()
        .extract()
        .response();
  }
}
