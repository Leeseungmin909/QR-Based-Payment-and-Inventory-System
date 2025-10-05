package min.example.QRp.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int purchaseItemId; // pk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseId")
    private Purchase purchase; // fk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId")
    private Product product; // fk

    private int orderQuantity;
    private int orderPrice;

    /** 정적 팩토리 메소드
     * 영수증의 한 줄, 제품 구매시 product의 제품이 주문수량만큼 빠짐
     * @param product 구매할 제품
     * @param orderPrice 제품 가격
     * @param orderQuantity 구매할 제품 수량
     * @return 구매한 purchaseItem 객체
     */
    public static PurchaseItem createPurchaseItem(Product product, int orderPrice, int orderQuantity) {
        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.product = product;
        purchaseItem.orderPrice = orderPrice;
        purchaseItem.orderQuantity = orderQuantity;

        product.removeStock(orderQuantity);

        return purchaseItem;
    }

    /** 연관관계 편의 메소드
     * purchaseItem을 purchase와 연결
     * @param purchase 이 항목이 속할 purchase 객체
     */
    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }
}
