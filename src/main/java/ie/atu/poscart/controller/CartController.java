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
    @PostMapping("/item/remove")
    public ResponseEntity<String> removeItem(@RequestBody AddItemRequest req) {
        try {
            cartService.removeItem(req.getBuyerUsername(), req.getProductId());
            return ResponseEntity.ok("Item successfully removed from the cart!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{buyerUsername}")
    public ResponseEntity<?> viewCart(@PathVariable String buyerUsername) {
        try {
            Cart cart = cartService.getCart(buyerUsername);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Cart not found for user: " + buyerUsername);
        }
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