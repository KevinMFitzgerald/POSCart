package ie.atu.poscart.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", url = "http://localhost:8080")
public interface InventoryClient {

    @GetMapping("/api/inventory/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long productId);

    @Data
    class ProductDto {
        private Long id;
        private String name;
        private double price;
        private int quantity;
    }
}
