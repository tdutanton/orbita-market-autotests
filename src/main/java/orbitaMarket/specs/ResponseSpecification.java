package orbitaMarket.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;

public class ResponseSpecification {

  public static io.restassured.specification.ResponseSpecification expectedStatusCode(int code) {
    return new ResponseSpecBuilder()
        .expectStatusCode(code)
        .expectContentType(ContentType.JSON)
        .build();
  }

  public static io.restassured.specification.ResponseSpecification expectedStatusCodeWithoutResponse(
      int code) {
    return new ResponseSpecBuilder()
        .expectStatusCode(code)
        .build();
  }

}
