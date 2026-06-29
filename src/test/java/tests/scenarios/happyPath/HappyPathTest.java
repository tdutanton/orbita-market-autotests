package tests.scenarios.happyPath;

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

@Feature("HappyPathTest")
public class HappyPathTest extends BaseTest {

  @Test
  @DisplayName("Positive [статус PAID, обновленный баланс] - счет -> пополнение 1000 -> заказ")
  void happyPathTest() {
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
  }

}
