package ie.atu.poscart.service;

import ie.atu.poscart.client.InventoryClient;
import ie.atu.poscart.client.PaymentClient;
import ie.atu.poscart.client.PaymentClient.PurchaseRequest;
import ie.atu.poscart.client.PaymentClient.PurchaseRequest.ItemDto;
import ie.atu.poscart.model.Cart;
import ie.atu.poscart.model.CartItem;
import ie.atu.poscart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;

    public Cart createOrFetchCart(String buyerUsername) {
        return cartRepo.findByBuyerUsername(buyerUsername)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setBuyerUsername(buyerUsername);
                    return cartRepo.save(cart);
                });
    }

    public Cart addItem(String buyerUsername, Long productId, int quantity) {
        Cart cart = createOrFetchCart(buyerUsername);

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getProductId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        return cartRepo.save(cart);
    }

    public Cart getCart(String buyerUsername) {
        return cartRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + buyerUsername));
    }

    @Transactional
    public String checkout(String buyerUsername) {
        Cart cart = cartRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("No cart found for user: " + buyerUsername));

        if (cart.getItems().isEmpty()) {
            return "Cart is empty!";
        }

        double totalCost = 0.0;
        List<ItemDto> purchaseItems = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            InventoryClient.ProductDto productDto = inventoryClient.getProductById(item.getProductId());

            if (productDto.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + productDto.getName());
            }
            totalCost += productDto.getPrice() * item.getQuantity();

            ItemDto purchaseItem = new ItemDto();
            purchaseItem.setProductId(item.getProductId());
            purchaseItem.setQuantity(item.getQuantity());
            purchaseItems.add(purchaseItem);
        }

        PurchaseRequest paymentRequest = new PurchaseRequest();
        paymentRequest.setBuyerUsername(buyerUsername);
        paymentRequest.setItems(purchaseItems);
        paymentRequest.setTotalCost(totalCost);

        String paymentResponse = paymentClient.purchase(paymentRequest);

        if (!paymentResponse.startsWith("Purchase successful")) {
            throw new RuntimeException("Payment failed: " + paymentResponse);
        }

        cartRepo.delete(cart);
        return "Checkout successful! Total cost: " + totalCost;
    }
}
