package orbitaMarket.service;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;

public class OrdersApi {

  private static final String BASE_PATH = "/orders";

  public Response createOrder(String userId, String productType, Map<String, Object> payload) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .body(Map.of(
            "productType", productType,
            "payload", payload
        ))
        .when()
        .post(BASE_PATH + "/orders")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response createOrderWithId(String userId, String orderId, String productType,
      Map<String, Object> payload) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .body(Map.of(
            "orderId", orderId,
            "productType", productType,
            "payload", payload
        ))
        .when()
        .post(BASE_PATH + "/orders")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response createOrderWithoutUserId(String productType,
      Map<String, Object> payload) {
    return given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "product_type", productType,
            "payload", payload
        ))
        .when()
        .post(BASE_PATH + "/orders")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response listOrders(String userId) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .when()
        .get(BASE_PATH + "/orders")
        .then()
        .log().body()
        .extract()
        .response();
  }

  public Response getOrder(String userId, String orderId) {
    return given()
        .contentType(ContentType.JSON)
        .header("X-User-Id", userId)
        .when()
        .get(BASE_PATH + "/{order_id}", orderId)
        .then()
        .log().body()
        .extract()
        .response();
  }
}
