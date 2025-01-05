package ie.atu.poscart.repository;

import ie.atu.poscart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByBuyerUsername(String buyerUsername);
}