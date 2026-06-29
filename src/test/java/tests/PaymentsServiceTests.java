package tests;

import static org.assertj.core.api.Assertions.assertThat;

import base.BaseTest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;
import orbitaMarket.model.AccountResponse;
import orbitaMarket.model.BalanceResponse;
import orbitaMarket.model.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("Payments Service")
class PaymentsServiceTests extends BaseTest {

  @Test
  @DisplayName("[201] POST /accounts - создать счет")
  void createAccountSuccess() {
    String userId = uniqueUserId();

    Response response = paymentsApi.createAccount(userId);
    response.then().statusCode(201);

    AccountResponse account = response.as(AccountResponse.class);
    assertThat(account.getUserId()).isEqualTo(userId);
    assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("[200/409] POST /accounts - повторное создание того же счета")
  void createAccountDuplicate() {
    String userId = uniqueUserId();

    paymentsApi.createAccount(userId).then().statusCode(201);
    Response duplicate = paymentsApi.createAccount(userId);

    int status = duplicate.statusCode();
    assertThat(status).isIn(200, 409);

    if (status == 409) {
      ErrorResponse error = duplicate.as(ErrorResponse.class);
      assertThat(error.getErrorCode()).isEqualTo("ACCOUNT_ALREADY_EXISTS");
    }
  }

  @Test
  @DisplayName("[400] POST /accounts - без X-User-Id")
  void createAccountMissingUserId() {
    Response response = paymentsApi.createAccountWithoutUserId();
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("MISSING_USER_ID");
  }

  @Test
  @DisplayName("[400] POST /accounts/top-up - без X-User-Id")
  void topUpMissingUserId() {
    Response response = paymentsApi.topUpWithoutUserId(100);
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("MISSING_USER_ID");
  }

  @Test
  @DisplayName("[200] POST /accounts/top-up - успешное пополнение")
  void topUpSuccess() {
    String userId = uniqueUserId();
    Response response = paymentsApi.createAccount(userId);
    response.then().statusCode(201);

    Response topUpResponse = paymentsApi.topUp(userId, BigDecimal.valueOf(500.0));
    topUpResponse.then().statusCode(200);
  }

  @Test
  @DisplayName("[404] POST /accounts/top-up - счет не найден")
  void topUpAccountNotFound() {
    Response response = paymentsApi.topUp("nonexistent-user", BigDecimal.valueOf(100.0));
    response.then().statusCode(404);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("ACCOUNT_NOT_FOUND");
  }

  @Test
  @DisplayName("[400] POST /accounts/top-up - некорректная сумма (0)")
  void topUpInvalidAmountZero() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId).then().statusCode(201);

    Response response = paymentsApi.topUp(userId, BigDecimal.valueOf(0.0));
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("INVALID_AMOUNT");
  }

  @Test
  @DisplayName("[400] POST /accounts/top-up - некорректная сумма (отрицательная)")
  void topUpInvalidAmountNegative() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId).then().statusCode(201);

    Response response = paymentsApi.topUp(userId, BigDecimal.valueOf(-50.0));
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("INVALID_AMOUNT");
  }

  @Test
  @DisplayName("[400] POST /accounts/top-up - сумма не число")
  void topUpInvalidAmountNotNumber() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId).then().statusCode(201);

    Response response = paymentsApi.topUpWithInvalidBody(userId, Map.of("amount", "abc"));
    response.then().statusCode(500);
  }

  @Test
  @DisplayName("[200] GET /accounts/balance - получить баланс")
  void getBalanceSuccess() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId).then().statusCode(201);
    paymentsApi.topUp(userId, BigDecimal.valueOf(300.0)).then().statusCode(200);

    Response response = paymentsApi.getBalance(userId);
    response.then().statusCode(200);

    BalanceResponse balance = response.as(BalanceResponse.class);
    assertThat(balance.getUserId()).isEqualTo(userId);
    assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(300));
  }

  @Test
  @DisplayName("[404] GET /accounts/balance - счет не найден")
  void getBalanceAccountNotFound() {
    Response response = paymentsApi.getBalance("nonexistent-user");
    response.then().statusCode(404);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("ACCOUNT_NOT_FOUND");
  }

  @Test
  @DisplayName("[400] GET /accounts/balance - без X-User-Id")
  void getBalanceMissingUserId() {
    Response response = paymentsApi.getBalanceWithoutUserId();
    response.then().statusCode(400);

    ErrorResponse error = response.as(ErrorResponse.class);
    assertThat(error.getErrorCode()).isEqualTo("MISSING_USER_ID");
  }

  @Test
  @DisplayName("Баланс после последовательных пополнений")
  void balanceAfterMultipleTopUps() {
    String userId = uniqueUserId();
    paymentsApi.createAccount(userId).then().statusCode(201);

    paymentsApi.topUp(userId, BigDecimal.valueOf(100.0)).then().statusCode(200);
    paymentsApi.topUp(userId, BigDecimal.valueOf(200.0)).then().statusCode(200);
    paymentsApi.topUp(userId, BigDecimal.valueOf(300.00)).then().statusCode(200);

    Response response = paymentsApi.getBalance(userId);
    BalanceResponse balance = response.as(BalanceResponse.class);
    assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(600));
  }
}
