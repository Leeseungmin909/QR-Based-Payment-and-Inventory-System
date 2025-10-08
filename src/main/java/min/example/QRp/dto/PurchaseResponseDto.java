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

    // 거래 정보 (구매 id, 구매시간, 구매상태)
    private final int purchaseId;
    private final LocalDateTime purchaseDate;
    private final PurchaseState state;

    // 구매한 상품 목록
    private final List<PurchaseItemDto> purchaseItems;

    // 구매한 모든 제품 결제 금액
    private final int totalAmount;

    public PurchaseResponseDto(Purchase purchase) {
        this.purchaseId = purchase.getPurchaseId();
        this.purchaseDate = purchase.getPurchaseDate();
        this.state = purchase.getState();

        // List<PurchaseItem>을 List<PurchaseItemDto>로 변환
        this.purchaseItems = purchase.getPurchaseItems().stream()
                .map(PurchaseItemDto::new) // 각 PurchaseItem을 PurchaseItemDto로 변환
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