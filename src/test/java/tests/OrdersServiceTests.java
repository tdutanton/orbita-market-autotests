package tests;

import static org.assertj.core.api.Assertions.assertThat;

import base.BaseTest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import orbitaMarket.model.ErrorResponse;
import orbitaMarket.model.OrderResponse;
import orbitaMarket.model.PayloadRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("Orders Service")
class OrdersServiceTests extends BaseTest {

  @Test
  @DisplayName("[201] POST /orders - создать ARCHIVE заказ")
  void createArchiveOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Response response = ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());
    response.then().statusCode(201);

    OrderResponse order = response.as(OrderResponse.class);
    assertThat(order.getOrderId()).isNotBlank();
    assertThat(order.getProductType()).isEqualTo("ARCHIVE");
    assertThat(order.getStatus()).isIn("CREATED", "PAYMENT_PENDING");
  }

  @Test
  @DisplayName("[201] POST /orders - создать MONITORING заказ")
  void createMonitoringOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Map<String, Object> payload = Map.of(
        "aoi", 5.0,
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

  @Test
  @DisplayName("[400] POST /orders - без X-User-Id")
  void createOrderMissingUserId() {

    Response response = ordersApi.createOrderWithoutUserId("ARCHIVE",
        PayloadRequest.archivePayload());
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("MISSING_USER_ID");
  }

  @Test
  @DisplayName("[200] GET /orders - список заказов пользователя")
  void listOrdersSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);
    paymentsApi.topUp(userId, BigDecimal.valueOf(1000.0));

    ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());
    ordersApi.createOrder(userId, "TASKING", PayloadRequest.taskingPayload());

    Response response = ordersApi.listOrders(userId);
    response.then().statusCode(200);

    List<OrderResponse> orders = response.jsonPath().getList("orders", OrderResponse.class);
    assertThat(orders).isNotEmpty();
    int total = response.jsonPath().getInt("total");
    assertThat(total).isEqualTo(2);
  }

  @Test
  @DisplayName("[200] GET /orders/{order_id} - детали заказа")
  void getOrderSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);

    Response created = ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());
    String orderId = created.jsonPath().getString("order_id");

    Response response = ordersApi.getOrder(userId, orderId);
    response.then().statusCode(200);

    OrderResponse order = response.as(OrderResponse.class);
    assertThat(order.getOrderId()).isEqualTo(orderId);
    assertThat(order.getProductType()).isEqualTo("ARCHIVE");

  }

  @Test
  @DisplayName("[404] GET /orders/{order_id} - заказ не найден")
  void getOrderNotFound() {
    String userId = uniqueUserId();
    Response response = ordersApi.getOrder(userId, "nonexistent-order");
    response.then().statusCode(404);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("ORDER_NOT_FOUND");
  }
}
