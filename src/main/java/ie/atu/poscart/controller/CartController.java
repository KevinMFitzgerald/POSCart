package ie.atu.poscart.controller;


import ie.atu.poscart.model.Cart;
import ie.atu.poscart.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080","http://localhost:8081"})
public class CartController {

    private final CartService cartService;

    @PostMapping("/create")
    public ResponseEntity<Cart> createCart(@RequestBody CreateCartRequest req) {
        Cart cart = cartService.createOrFetchCart(req.getBuyerUsername());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/item")
    public ResponseEntity<Cart> addItem(@RequestBody AddItemRequest req) {
        Cart cart = cartService.addItem(req.getBuyerUsername(), req.getProductId(), req.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/{buyerUsername}")
    public ResponseEntity<Cart> viewCart(@PathVariable String buyerUsername) {
        Cart cart = cartService.getCart(buyerUsername);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestBody Map<String, String> payload) {
        String buyerUsername = payload.get("buyerUsername");
        if (buyerUsername == null) {
            return ResponseEntity.badRequest().body("Missing buyerUsername");
        }
        return ResponseEntity.ok(cartService.checkout(buyerUsername));
    }

    @Data
    static class CreateCartRequest {
        private String buyerUsername;
    }

    @Data
    static class AddItemRequest {
        private String buyerUsername;
        private Long productId;
        private int quantity;
    }

    @Data
    static class CheckoutRequest {
        private String buyerUsername;
    }
}