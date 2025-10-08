package min.example.QRp.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseItemTest {

    @Test
    void 구매항목이_생성되고_재고가_차감된다() {
        // given
        Product apple = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(10)
                .build();

        // when
        PurchaseItem item = PurchaseItem.createPurchaseItem(apple, 1000, 3);

        // then
        assertEquals(apple, item.getProduct());
        assertEquals(1000, item.getOrderPrice());
        assertEquals(3, item.getOrderQuantity());
        assertEquals(7, apple.getQuantity()); // 재고 차감 확인
    }

    @Test
    void 구매항목이_구매와_연결된다() {
        // given
        Product banana = Product.builder()
                .name("바나나")
                .price(500)
                .quantity(20)
                .build();

        PurchaseItem item = PurchaseItem.createPurchaseItem(banana, 500, 5);
        Purchase purchase = new Purchase();

        // when
        item.setPurchase(purchase);

        // then
        assertEquals(purchase, item.getPurchase());
    }
}
