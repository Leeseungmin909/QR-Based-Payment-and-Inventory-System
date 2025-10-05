package min.example.QRp.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId; // pk

    private String name;
    private int price;
    private int quantity;

    @Builder
    public Product(String name, int price , int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    /** 이름,가격,수량을 변경하고싶지 않다면 dto 에서 null 값을 받아 if 문을 통과시킴
     * 제품 수정
     * @param name 변경할 제품 이름
     * @param price 변경할 제품 가격
     * @param quantity 변경할 제품 수량
     */
    public void update(String name, Integer price, Integer quantity) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (price != null && price >= 0) {
            this.price = price;
        }
        if (quantity != null && quantity >= 0) {
            this.quantity = quantity;
        }
    }

    /**
     * 제품 차감 (구매 시 사용)
     * @param quantity 차감할 수량
     */
    public void removeStock(int quantity){
        int restStock = this.quantity - quantity;
        if (restStock < 0) {
            throw new IllegalStateException("재고가 부족합니다. (현재 재고: " + this.quantity + ")");
        }
        this.quantity = restStock;
    }

    /**
     * 재고 추가 (환불 시 사용)
     * @param quantity 추가할 수량
     */
    public void addStock(int quantity) {
        this.quantity += quantity;
    }

}
