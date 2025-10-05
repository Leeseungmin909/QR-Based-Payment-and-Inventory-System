package min.example.QRp.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access =  AccessLevel.PROTECTED)
@Getter
@Entity
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int  purchaseId; // pk

    @Enumerated(EnumType.STRING)
    private PurchaseState state; // 구매,환불 등의 상태를 나타냄

    @CreationTimestamp
    private LocalDateTime purchaseDate; // 구매시간

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL)
    private List<PurchaseItem> purchaseItems = new ArrayList<>(); // purchaseItem 과 1:N 관계

    public void changeState(PurchaseState newState){
        this.state = newState;
    }

    /**
     * db가 저장되기전에 구매시간, 제품상태를 초기설정
     */
    @PrePersist
    public void onPrePersist() {
        this.purchaseDate = LocalDateTime.now();
        this.state = PurchaseState.COMPLETED;
    }

    /** 연관관계 편의 메소드, 데이터 불일치를 막기 위해 사용
     * 구매 목록에 구매한 제품을 담는 기능
     * @param purchaseItem 구매한 제품
     */
    public void addPurchaseItem(PurchaseItem purchaseItem) {
        purchaseItems.add(purchaseItem);
        purchaseItem.setPurchase(this);
    }

    /** 정적 팩토리 메소드
     * purchase 객체를 만들어 구매한 제품을 새로만든 객체에 추가하는 기능
     * @param purchaseItems 구매한 제품들
     * @return 구매목록을 담은 purchase 객체
     */
    public static Purchase createPurchase(PurchaseItem... purchaseItems) {
        Purchase purchase = new Purchase();

        for (PurchaseItem purchaseItem : purchaseItems) {
            purchase.addPurchaseItem(purchaseItem);
        }

        return purchase;
    }

}
