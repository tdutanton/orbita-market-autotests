package orbitaMarket.client;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;

public class ApiClient {

  public Response get(String endpoint) {
    return given()
        .log().all()
        .then()
        .log().all()
        .when()
        .get(endpoint);
  }

  public Response get(String endpoint, Map<String, Object> queryParams) {
    return given()
        .log().all()
        .queryParams(queryParams)
        .when()
        .get(endpoint)
        .then()
        .log().all()
        .extract()
        .response();
  }

  public Response post(String endpoint, Map<String, Object> body) {
    return given()
        .log().all()
        .contentType(ContentType.JSON)
        .body(body)
        .then().log().all()
        .when()
        .post(endpoint);
  }

  public Response postWithPathParams(String endpoint, Map<String, Object> pathParams) {
    return given()
        .log().all()
        .pathParams(pathParams)
        .then().log().all()
        .when()
        .post(endpoint);
  }

  public Response postWithoutBody(String endpoint) {
    return given()
        .log().all()
        .then().log().all()
        .when()
        .post(endpoint);
  }

  public Response delete(String endpoint, Map<String, Object> pathParams) {
    return given()
        .log().all()
        .pathParams(pathParams)
        .when()
        .delete(endpoint)
        .then().log().all()
        .extract().response();
  }
}
