package ie.atu.poscart;

import ie.atu.poscart.client.InventoryClient;
import ie.atu.poscart.client.PaymentClient;
import ie.atu.poscart.model.Cart;
import ie.atu.poscart.model.CartItem;
import ie.atu.poscart.repository.CartRepository;
import ie.atu.poscart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;

    @BeforeEach
    public void setUp() {
        //Initialize mocks and pass them correctly to the CartService
        MockitoAnnotations.openMocks(this);
        cartService = new CartService(cartRepository, paymentClient, inventoryClient);

        testCart = new Cart();
        testCart.setBuyerUsername("testUser");
    }

    // Test Adding an Item to the Cart
    @Test
    public void testAddItemToCart() {
        when(cartRepository.findByBuyerUsername("testUser")).thenReturn(Optional.of(testCart));

        cartService.addItem("testUser", 1L, 2);

        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // Test Removing an Item from the Cart
    @Test
    public void testRemoveItemFromCart() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(2);
        testCart.getItems().add(item);

        when(cartRepository.findByBuyerUsername("testUser")).thenReturn(Optional.of(testCart));

        cartService.removeItem("testUser", 1L);

        assertEquals(0, testCart.getItems().size());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testCreateNewCartIfNotExists() {
        when(cartRepository.findByBuyerUsername("newUser")).thenReturn(Optional.empty());

        Cart cart = cartService.createOrFetchCart("newUser");

        assertNotNull(cart, "Cart should not be null");
        assertEquals("newUser", cart.getBuyerUsername());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
    @Test
    public void testCheckoutWithSufficientStock() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(2);
        testCart.getItems().add(item);
        when(cartRepository.findByBuyerUsername("testUser")).thenReturn(Optional.of(testCart));
        when(inventoryClient.getProductById(1L)).thenReturn(new InventoryClient.ProductDto(1L, "Test Product", 100.0, 10));
        when(paymentClient.purchase(any())).thenReturn("Purchase successful!");
        String result = cartService.checkout("testUser");
        assertTrue(result.startsWith("Checkout successful!"));
        verify(cartRepository, times(1)).delete(testCart);
    }
    @Test
    public void testCheckoutWithInsufficientStock() {
        CartItem item = new CartItem();
        item.setProductId(1L);
        item.setQuantity(5);
        testCart.getItems().add(item);

        when(cartRepository.findByBuyerUsername("testUser")).thenReturn(Optional.of(testCart));
        when(inventoryClient.getProductById(1L)).thenReturn(new InventoryClient.ProductDto(1L, "Test Product", 100, 2));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            cartService.checkout("testUser");
        });

        assertEquals("Not enough stock for product: Test Product", exception.getMessage());
    }
}
