package min.example.QRp.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTest {

    @Test
    void 상태가_변경된다() {
        // given
        Purchase purchase = new Purchase();

        // when
        purchase.changeState(PurchaseState.REFUNDED);

        // then
        assertEquals(PurchaseState.REFUNDED, purchase.getState());
    }

    @Test
    void 저장전_구매시간과상태가_자동설정된다() {
        // given
        Purchase purchase = new Purchase();

        // when
        purchase.onPrePersist();

        // then
        assertNotNull(purchase.getPurchaseDate());
        assertEquals(PurchaseState.COMPLETED, purchase.getState());
    }

    @Test
    void 구매목록에_제품이_추가된다() {
        // given
        Product product = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(10)
                .build();

        PurchaseItem purchaseItem = PurchaseItem.createPurchaseItem(product, 1000, 2);

        Purchase purchase = new Purchase();

        // when
        purchase.addPurchaseItem(purchaseItem);

        // then
        assertEquals(1, purchase.getPurchaseItems().size());
        assertTrue(purchase.getPurchaseItems().contains(purchaseItem));
        assertEquals(purchase, purchaseItem.getPurchase()); // 양방향 관계 확인
    }

    @Test
    void 구매목록을_생성할수있다() {
        // given
        Product apple = Product.builder()
                .name("사과")
                .price(500)
                .quantity(10)
                .build();
        Product banana = Product.builder()
                .name("바나나")
                .price(1000)
                .quantity(20)
                .build();

        PurchaseItem item1 = PurchaseItem.createPurchaseItem(apple, 500, 3);
        PurchaseItem item2 = PurchaseItem.createPurchaseItem(banana, 1000, 5);

        // when
        Purchase purchase = Purchase.createPurchase(item1, item2);

        // then
        assertEquals(2, purchase.getPurchaseItems().size());
        assertTrue(purchase.getPurchaseItems().contains(item1));
        assertTrue(purchase.getPurchaseItems().contains(item2));
        assertEquals(purchase, item1.getPurchase());
        assertEquals(purchase, item2.getPurchase());
    }
}
