package tests.scenarios.multipleOrders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import base.BaseTest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import orbitaMarket.model.BalanceResponse;
import orbitaMarket.model.PayloadRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("MultipleOrdersTest")
public class MultipleOrdersTest extends BaseTest {

  @Test
  @DisplayName("Два заказа positive [Итог = 2 * price, баланс не отрицательный] - счет -> пополнение 1000 -> заказ -> заказ -> баланс")
  void multipleOrdersTest() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId);
    paymentsApi.topUp(userId, BigDecimal.valueOf(1000.0));
    ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());
    ordersApi.createOrder(userId, "ARCHIVE", PayloadRequest.archivePayload());

    await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          Response userCheck = paymentsApi.getBalance(userId);
          BalanceResponse balance = userCheck.as(BalanceResponse.class);
          assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(400.0));
        });
  }
}
