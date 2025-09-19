package min.example.QRp.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int  purchaseId; // pk
    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product; // fk
    private int quantity;
    private String state; // 구매,환불 등의 상태를 나타냄
    @CreationTimestamp
    private LocalDateTime purchaseDate;
}
