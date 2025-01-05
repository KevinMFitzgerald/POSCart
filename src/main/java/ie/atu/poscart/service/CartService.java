package ie.atu.poscart.service;

import ie.atu.poscart.DTOs.CartDTO;
import ie.atu.poscart.DTOs.CartItemDTO;
import ie.atu.poscart.client.InventoryClient;
import ie.atu.poscart.client.PaymentClient;
import ie.atu.poscart.model.Cart;
import ie.atu.poscart.model.CartItem;
import ie.atu.poscart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;

    public Cart createOrFetchCart(String buyerUsername) {
        return cartRepo.findByBuyerUsername(buyerUsername)
                .orElseGet(() -> cartRepo.save(Cart.builder()
                        .buyerUsername(buyerUsername)
                        .build()));
    }

    public Cart addItem(String buyerUsername, Long productId, int quantity) {
        Cart cart = createOrFetchCart(buyerUsername);

        // see if item is already in cart
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .cart(cart)
                    .build();
            cart.getItems().add(newItem);
        }

        return cartRepo.save(cart);
    }

    public CartDTO getCart(String buyerUsername) {
        Cart cart = cartRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return mapToDTO(cart);  // Convert to DTO before returning
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setBuyerUsername(cart.getBuyerUsername());
        cartDTO.setItems(cart.getItems().stream().map(item -> {
            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setProductId(item.getProductId());
            itemDTO.setQuantity(item.getQuantity());
            return itemDTO;
        }).collect(Collectors.toList()));
        return cartDTO;
    }

    public String checkout(String buyerUsername) {
        Cart cart = cartRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("No cart for " + buyerUsername));

        if (cart.getItems().isEmpty()) {
            return "Cart is empty!";
        }

        for (CartItem item : cart.getItems()) {
            try {
                InventoryClient.ProductDto productDto = inventoryClient.getProductById(item.getProductId());
                if (productDto == null || productDto.getPrice() <= 0) {
                    return "Invalid product price for product ID: " + item.getProductId();
                }

                double cost = productDto.getPrice() * item.getQuantity();

                PaymentClient.PurchaseRequest req = new PaymentClient.PurchaseRequest();
                req.setBuyerUsername(buyerUsername);
                req.setProductId(Long.parseLong(item.getProductId().toString()));
                req.setTotalCost(cost);
                req.setQuantity(item.getQuantity());

                String payResponse = paymentClient.purchase(req);
                if (!payResponse.startsWith("Purchase successful")) {
                    return "Checkout failed on product " + item.getProductId() + ": " + payResponse;
                }

            } catch (Exception e) {
                return "Error during checkout for product ID: " + item.getProductId() + " - " + e.getMessage();
            }
        }

        cartRepo.delete(cart);
        return "Checkout success! All items purchased.";
    }



}
