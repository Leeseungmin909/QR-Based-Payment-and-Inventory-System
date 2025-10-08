package min.example.QRp.repository;

import min.example.QRp.domain.Product;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PurchaseRepositoryTest {

    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    ProductRepository productRepository; // Purchase 생성에 Product가 필요하므로 주입

    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 상품 미리 생성
        productA = new Product("상품A", 1000, 10);
        productB = new Product("상품B", 2000, 20);
        productRepository.create(productA);
        productRepository.create(productB);
    }

    @DisplayName("결제 기록을 저장하고 ID로 조회하면 해당 기록이 조회된다.")
    @Test
    void createAndFindById() {
        // given
        PurchaseItem itemA = PurchaseItem.createPurchaseItem(productA, productA.getPrice(), 2); // 상품A 2개 구매
        Purchase purchase = Purchase.createPurchase(itemA);

        // when
        purchaseRepository.create(purchase);
        Optional<Purchase> foundPurchaseOptional = purchaseRepository.findById(purchase.getPurchaseId());

        // then
        assertThat(foundPurchaseOptional).isPresent();
        Purchase foundPurchase = foundPurchaseOptional.get();
        assertThat(foundPurchase.getPurchaseId()).isEqualTo(purchase.getPurchaseId());
        assertThat(foundPurchase.getPurchaseItems()).hasSize(1);
        assertThat(foundPurchase.getPurchaseItems().get(0).getProduct().getName()).isEqualTo("상품A");
    }

    @DisplayName("전체 결제 기록을 조회하면 저장된 모든 기록이 반환된다.")
    @Test
    void findAll() {
        // given
        PurchaseItem itemA = PurchaseItem.createPurchaseItem(productA, productA.getPrice(), 1);
        Purchase purchase1 = Purchase.createPurchase(itemA);
        purchaseRepository.create(purchase1);

        PurchaseItem itemB = PurchaseItem.createPurchaseItem(productB, productB.getPrice(), 2);
        Purchase purchase2 = Purchase.createPurchase(itemB);
        purchaseRepository.create(purchase2);

        // when
        List<Purchase> allPurchases = purchaseRepository.findAll();

        // then
        assertThat(allPurchases).hasSize(2);
    }

    @DisplayName("특정 기간 사이의 결제 기록을 정확히 조회한다.")
    @Test
    void findByDateBetween() {
        // given
        // onPrePersist에 의해 purchaseDate는 자동으로 설정되므로 별도 설정 불필요
        PurchaseItem item1 = PurchaseItem.createPurchaseItem(productA, productA.getPrice(), 1);
        Purchase purchaseToday1 = Purchase.createPurchase(item1);
        purchaseRepository.create(purchaseToday1);

        PurchaseItem item2 = PurchaseItem.createPurchaseItem(productB, productB.getPrice(), 1);
        Purchase purchaseToday2 = Purchase.createPurchase(item2);
        purchaseRepository.create(purchaseToday2);

        // when
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Purchase> purchases = purchaseRepository.findByDateBetween(startOfDay, endOfDay);

        // then
        assertThat(purchases).hasSize(2);
    }

    @DisplayName("조회 기간에 결제 기록이 없으면 빈 리스트를 반환한다.")
    @Test
    void findByDateBetween_empty() {
        // given
        // 오늘 구매 기록은 저장하지만, 어제 날짜로 조회

        PurchaseItem item = PurchaseItem.createPurchaseItem(productA, productA.getPrice(), 1);
        Purchase purchaseToday = Purchase.createPurchase(item);
        purchaseRepository.create(purchaseToday);

        // when
        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfYesterday = LocalDateTime.now().minusDays(1).toLocalDate().atTime(23, 59, 59);
        List<Purchase> purchases = purchaseRepository.findByDateBetween(startOfYesterday, endOfYesterday);

        // then
        assertThat(purchases).isEmpty();
    }
}