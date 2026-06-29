package tests.scenarios.repeatSameOrderId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import base.BaseTest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import orbitaMarket.model.BalanceResponse;
import orbitaMarket.model.OrderResponse;
import orbitaMarket.model.PayloadRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("RepeatSameOrderIdTest")
public class RepeatSameOrderIdTest extends BaseTest {

  @Test
  @DisplayName("Повтор с тем же orderId [с баланса повторно не списаны деньги] - счет -> пополнение 1000 -> заказ -> заказ на тот же orderId -> баланс")
  void repeatSameOrderIdTest() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);
    paymentsApi.topUp(userId, BigDecimal.valueOf(1000.0));
    Response response = ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());

    String orderId = response.jsonPath().getString("order_id");

    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          Response orderCheck = ordersApi.getOrder(userId, orderId);
          OrderResponse order = orderCheck.as(OrderResponse.class);
          assertThat(order.getStatus()).isEqualTo("PAID");
        });
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          Response userCheck = paymentsApi.getBalance(userId);
          BalanceResponse balance = userCheck.as(BalanceResponse.class);
          assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700.0));
        });

    Response sameResponse = ordersApi.createOrderWithId(userId, orderId, "ARCHIVE",
        PayloadRequest.archivePayload());
    sameResponse.then().statusCode(500);
    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          Response userCheck = paymentsApi.getBalance(userId);
          BalanceResponse balance = userCheck.as(BalanceResponse.class);
          assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700.0));
        });
  }
}
