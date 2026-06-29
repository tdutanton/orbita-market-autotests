package tests.scenarios.TwoAccountsForOneUserTest;

import static org.assertj.core.api.Assertions.assertThat;

import base.BaseTest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import orbitaMarket.model.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("TwoAccountsForOneUser")
public class TwoAccountsForOneUserTest extends BaseTest {

  @Test
  @DisplayName("Создать два счета для одного user [Итог 500] - счет -> счет")
  void TwoAccountsForOneUserTest() {
    String userId = uniqueUserId();

    paymentsApi.createAccount(userId).then().statusCode(201);
    Response duplicate = paymentsApi.createAccount(userId);

    int status = duplicate.statusCode();
    assertThat(status).isEqualTo(409);
    ErrorResponse error = duplicate.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("ACCOUNT_ALREADY_EXISTS");
  }
}
