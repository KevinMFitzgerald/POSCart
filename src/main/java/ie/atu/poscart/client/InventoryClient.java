package ie.atu.poscart.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", url = "http://localhost:8080")
public interface InventoryClient {

    @GetMapping("/api/inventory/products/{id}")
    ProductDto getProductById(@PathVariable Long id);

    @PutMapping("/api/inventory/products/{id}/decrement")
    String decrementStock(@PathVariable("id") Long id, @RequestBody DecrementRequest request);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ProductDto {
        private Long id;
        private String name;
        private double price;
        private int quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class DecrementRequest {
        private int amount;
    }
}
