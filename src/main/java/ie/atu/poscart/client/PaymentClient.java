package ie.atu.poscart.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.Data;

import java.util.List;

@FeignClient(name = "payment-service", url = "http://localhost:8081")
public interface PaymentClient {

    @PostMapping("/api/payment/purchase")
    String purchase(@RequestBody PurchaseRequest req);

    @Data
    class PurchaseRequest {
        private String buyerUsername;
        private List<ItemDto> items;
        private double totalCost;

        @Data
        public static class ItemDto {
            private Long productId;
            private int quantity;
        }
    }
}
