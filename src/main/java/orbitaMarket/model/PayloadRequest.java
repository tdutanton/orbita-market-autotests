package orbitaMarket.model;

import java.util.Map;

public class PayloadRequest {

  public static Map<String, Object> archivePayload() {
    return Map.of(
        "aoi", 3.0,
        "capture_date", "2026-06-01",
        "sensor_type", "MSI"
    );
  }

  public static Map<String, Object> taskingPayload() {
    return Map.of(
        "aoi", 3.0,
        "time_window", Map.of("from", "2026-07-01", "to", "2026-07-15"),
        "sensor_type", "MSI"
    );
  }

}
