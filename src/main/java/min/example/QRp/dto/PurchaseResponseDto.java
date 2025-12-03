package min.example.QRp.dto;

import lombok.Getter;
import lombok.Setter;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import min.example.QRp.domain.PurchaseState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PurchaseResponseDto {

    private final int purchaseId;
    private final LocalDateTime purchaseDate;
    private final PurchaseState state;
    private final List<PurchaseItemDto> purchaseItems;
    private final int totalAmount;

    public PurchaseResponseDto(Purchase purchase) {
        this.purchaseId = purchase.getPurchaseId();
        this.purchaseDate = purchase.getPurchaseDate();
        this.state = purchase.getState();
        this.purchaseItems = purchase.getPurchaseItems().stream()
                .map(PurchaseItemDto::new)
                .collect(Collectors.toList());
        this.totalAmount = this.purchaseItems.stream()
                .mapToInt(item -> item.getOrderPrice() * item.getOrderQuantity())
                .sum();
    }

    /**
     * 영수증의 각 항목을 표현하는 내부 DTO
     */
    @Getter
    private static class PurchaseItemDto {
        private final String productName;
        private final int orderPrice;
        private final int orderQuantity;
        private final int itemTotalAmount;

        public PurchaseItemDto(PurchaseItem item) {
            this.productName = item.getProduct().getName();
            this.orderPrice = item.getOrderPrice();
            this.orderQuantity = item.getOrderQuantity();
            this.itemTotalAmount = this.orderPrice * this.orderQuantity;
        }
    }
}