package tests;

import static org.assertj.core.api.Assertions.assertThat;

import base.BaseTest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import orbitaMarket.model.ErrorResponse;
import orbitaMarket.model.OrderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Feature("Orders Service")
class OrdersServiceTests extends BaseTest {

  @Test
  @DisplayName("[201] POST /orders - создать ARCHIVE заказ")
  void createArchiveOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Map<String, Object> payload = Map.of(
        "aoi", 3.0,
        "capture_date", "2026-06-01",
        "sensor_type", "MSI"
    );

    Response response = ordersApi.createOrder(userId, "ARCHIVE", payload);
    response.then().statusCode(201);

    OrderResponse order = response.as(OrderResponse.class);
    assertThat(order.getOrderId()).isNotBlank();
    assertThat(order.getProductType()).isEqualTo("ARCHIVE");
    assertThat(order.getStatus()).isIn("CREATED", "PAYMENT_PENDING");
  }

  @Test
  @DisplayName("[201] POST /orders - создать TASKING заказ")
  void createTaskingOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Map<String, Object> payload = Map.of(
        "aoi", "{\"type\":\"Polygon\",\"coordinates\":[[[30,60],[31,60],[31,61],[30,61],[30,60]]]}",
        "time_window", Map.of("from", "2026-07-01", "to", "2026-07-15"),
        "sensor_type", "SAR"
    );

    Response response = ordersApi.createOrder(userId, "TASKING", payload);
    response.then().statusCode(201);

    OrderResponse order = response.as(OrderResponse.class);
    assertThat(order.getProductType()).isEqualTo("TASKING");
    assertThat(order.getStatus()).isIn("CREATED", "PAYMENT_PENDING");
  }

  @Test
  @DisplayName("[201] POST /orders - создать MONITORING заказ")
  void createMonitoringOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Map<String, Object> payload = Map.of(
        "aoi", "{\"type\":\"Polygon\",\"coordinates\":[[[30,60],[31,60],[31,61],[30,61],[30,60]]]}",
        "cadence", "DAILY",
        "duration_days", 30
    );

    Response response = ordersApi.createOrder(userId, "MONITORING", payload);
    response.then().statusCode(201);

    OrderResponse order = response.as(OrderResponse.class);
    assertThat(order.getProductType()).isEqualTo("MONITORING");
    assertThat(order.getStatus()).isIn("CREATED", "PAYMENT_PENDING");
  }

  @Test
  @DisplayName("[400] POST /orders - неизвестный product_type")
  void createOrderUnknownProductType() {
    String userId = uniqueUserId();

    Response response = ordersApi.createOrder(userId, "INVALID_TYPE", Map.of("aoi", "test"));
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("UNKNOWN_PRODUCT_TYPE");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -100})
  @DisplayName("[400] POST /orders - некорректная цена")
  void createOrderInvalidPrice(int invalidPrice) {
    String userId = uniqueUserId();

    Map<String, Object> payload = Map.of(
        "aoi", "test-aoi",
        "capture_date", "2026-06-01",
        "sensor_type", "OPTICAL"
    );

    Response response = ordersApi.createOrder(userId, "ARCHIVE", payload);
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("INVALID_PRICE");
  }

  @Test
  @DisplayName("[400] POST /orders - без X-User-Id")
  void createOrderMissingUserId() {
    Map<String, Object> payload = Map.of(
        "aoi", "test-aoi",
        "capture_date", "2026-06-01",
        "sensor_type", "OPTICAL"
    );

    Response response = ordersApi.createOrderWithoutUserId("ARCHIVE", 100, payload);
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("MISSING_USER_ID");
  }

  @Test
  @DisplayName("[200] GET /orders - список заказов пользователя")
  void listOrdersSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    ordersApi.createOrder(userId, "ARCHIVE", archivePayload());
    ordersApi.createOrder(userId, "TASKING", taskingPayload());

    Response response = ordersApi.listOrders(userId);
    response.then().statusCode(200);

    List<Map<String, Object>> orders = response.jsonPath().getList(".");
    assertThat(orders).isNotEmpty();
  }

  @Test
  @DisplayName("[200] GET /orders/{order_id} - детали заказа")
  void getOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Response created = ordersApi.createOrder(userId, "ARCHIVE", archivePayload());
    String orderId = created.jsonPath().getString("order_id");

    Response response = ordersApi.getOrder(userId, orderId);
    response.then().statusCode(200);

    OrderResponse order = response.as(OrderResponse.class);
    assertThat(order.getOrderId()).isEqualTo(orderId);
    assertThat(order.getUserId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("[404] GET /orders/{order_id} - заказ не найден")
  void getOrderNotFound() {
    Response response = ordersApi.getOrder("some-user", "nonexistent-order");
    response.then().statusCode(404);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("ORDER_NOT_FOUND");
  }

  private Map<String, Object> archivePayload() {
    return Map.of(
        "aoi", "{\"type\":\"Polygon\",\"coordinates\":[[[30,60],[31,60],[31,61],[30,61],[30,60]]]}",
        "capture_date", "2026-06-01",
        "sensor_type", "OPTICAL"
    );
  }

  private Map<String, Object> taskingPayload() {
    return Map.of(
        "aoi", "{\"type\":\"Polygon\",\"coordinates\":[[[30,60],[31,60],[31,61],[30,61],[30,60]]]}",
        "time_window", Map.of("from", "2026-07-01", "to", "2026-07-15"),
        "sensor_type", "SAR"
    );
  }
}
