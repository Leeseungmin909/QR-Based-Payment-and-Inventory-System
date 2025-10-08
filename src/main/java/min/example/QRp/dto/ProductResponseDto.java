package min.example.QRp.dto;

import lombok.Getter;
import min.example.QRp.domain.Product;

@Getter
public class ProductResponseDto {
    private final int productId;
    private final String name;
    private final int price;
    private final int quantity;

    public ProductResponseDto(Product product) {
        this.productId = product.getProductId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.quantity = product.getQuantity();
    }
}
