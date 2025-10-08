package min.example.QRp.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void 업데이트_정상적으로_변경된다() {
        // given
        Product product = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(10)
                .build();

        // when
        product.update("바나나", 2000, 20);

        // then
        assertEquals("바나나", product.getName());
        assertEquals(2000, product.getPrice());
        assertEquals(20, product.getQuantity());
    }

    @Test
    void 업데이트_null값은_무시된다() {
        Product product = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(10)
                .build();

        product.update("배", null, null);

        assertEquals("배", product.getName());
        assertEquals(1000, product.getPrice());
        assertEquals(10, product.getQuantity());
    }

    @Test
    void 재고차감_정상적으로_줄어든다() {
        Product product = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(10)
                .build();

        product.removeStock(3);

        assertEquals(7, product.getQuantity());
    }

    @Test
    void 재고차감_부족하면_예외발생() {
        Product product = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(2)
                .build();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> product.removeStock(5)
        );
        assertTrue(exception.getMessage().contains("재고가 부족합니다"));
    }

    @Test
    void 재고추가_정상적으로_늘어난다() {
        Product product = Product.builder()
                .name("사과")
                .price(1000)
                .quantity(5)
                .build();

        product.addStock(3);

        assertEquals(8, product.getQuantity());
    }
}
