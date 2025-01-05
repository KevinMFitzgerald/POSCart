package ie.atu.poscart.client;


import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", url = "http://localhost:8081")
public interface PaymentClient {

    @PostMapping("/api/payment/purchase")
    String purchase(@RequestBody PurchaseRequest req);

    @Data
    class PurchaseRequest {
        private String buyerUsername;
        private Long productId;
        private double totalCost;
        private int quantity;
    }
}