package orbitaMarket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {

  @JsonProperty("order_id")
  private String orderId;

  @JsonProperty("product_type")
  private String productType;

  private String status;

  private BigDecimal price;

  private Map<String, Object> payload;

  @JsonProperty("failure_reason")
  private String failureReason;

  @JsonProperty("created_at")
  private String createdAt;
}
