package tests.scenarios.noCash;

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
public class NoCashTest extends BaseTest {

  @Test
  @DisplayName("Недостаточно средств [check - статус PAYMENT_FAILED, баланс тот же] - счет -> пополнение 50 -> заказ больше баланса")
  void noCashTest() {
    String userId = uniqueUserId();
    BigDecimal topUpBalance = BigDecimal.valueOf(50.0);
    paymentsApi.createAccount(userId);
    paymentsApi.topUp(userId, topUpBalance);
    Response response = ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());

    String orderId = response.jsonPath().getString("order_id");

    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          Response orderCheck = ordersApi.getOrder(userId, orderId);
          OrderResponse order = orderCheck.as(OrderResponse.class);
          assertThat(order.getStatus()).isEqualTo("PAYMENT_FAILED");
        });

    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          Response userCheck = paymentsApi.getBalance(userId);
          BalanceResponse balance = userCheck.as(BalanceResponse.class);
          assertThat(balance.getBalance()).isEqualByComparingTo(topUpBalance);
        });
  }

}
