package ie.atu.poscart.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import lombok.Data;

@FeignClient(name = "inventory-service", url = "http://localhost:8080")
public interface InventoryClient {

    @GetMapping("/api/inventory/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long productId);

    @PutMapping("/api/inventory/products/{id}/decrement")
    String decrementStock(@PathVariable("id") Long productId, @RequestBody DecrementRequest request);

    @Data
    class DecrementRequest {
        private int amount;
    }

    @Data
    class ProductDto {
        private Long id;
        private String name;
        private double price;
        private int quantity;
    }
}
