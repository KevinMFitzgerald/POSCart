package ie.atu.poscart.DTOs;


import lombok.Data;

@Data
public class CartItemDTO {
    private Long productId;
    private int quantity;
}