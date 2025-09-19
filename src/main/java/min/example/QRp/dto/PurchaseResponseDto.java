package min.example.QRp.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.ToString;
import min.example.QRp.domain.Purchase;

@ToString
@Getter
public class PurchaseResponseDto {
    private final int purchaseId;
    private final int productId;
    private final int purchaseQuantity;
    private final String name;
    private final int price;
    private final String state;
    private final LocalDateTime purchaseDate;

    public PurchaseResponseDto(Purchase purchase){
        this.purchaseId = purchase.getPurchaseId();
        this.productId = purchase.getProduct().getProductId();
        this.purchaseQuantity = purchase.getQuantity();
        this.name = purchase.getProduct().getName();
        this.price = purchase.getProduct().getPrice();
        this.state = purchase.getState();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.purchaseDate = LocalDateTime.parse(purchase.getPurchaseDate().format(formatter));
    }
}