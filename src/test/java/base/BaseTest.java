package base;

import io.restassured.RestAssured;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import orbitaMarket.client.ApiClient;
import orbitaMarket.config.TestConfig;
import orbitaMarket.service.OrdersApi;
import orbitaMarket.service.PaymentsApi;

public class BaseTest {

    public static ApiClient apiClient;
    public static PaymentsApi paymentsApi;
    public static OrdersApi ordersApi;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = TestConfig.BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        apiClient = new ApiClient();
        paymentsApi = new PaymentsApi();
        ordersApi = new OrdersApi();
    }

    protected static String uniqueUserId() {
        return "test-user-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
