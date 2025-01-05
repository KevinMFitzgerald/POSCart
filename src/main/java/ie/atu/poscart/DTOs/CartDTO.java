package ie.atu.poscart.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class CartDTO {
    private String buyerUsername;
    private List<CartItemDTO> items;
}