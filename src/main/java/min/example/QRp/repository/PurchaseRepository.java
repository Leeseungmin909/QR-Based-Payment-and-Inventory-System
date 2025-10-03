package min.example.QRp.repository;

import jakarta.persistence.EntityManager;
import min.example.QRp.domain.Purchase;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PurchaseRepository {
    private final EntityManager em;

    public PurchaseRepository(EntityManager em) {
        this.em = em;
    }

    /**
     * 구매 고유번호 조회
     * @param id 구매 고유 아이디
     * @return
     */
    public Optional<Purchase> findById(int id){
        Purchase purchase = em.find(Purchase.class,id);
        return Optional.ofNullable(purchase);
    }

    /**
     * 결제기록 저장
     * @param purchase
     * @return
     */
    public Purchase save(Purchase purchase) {
        if (purchase.getPurchaseId() == 0) {
            em.persist(purchase); // 신규 등록
            return purchase;
        } else {
            return em.merge(purchase); // 수정
        }
    }

    /**
     * 전체 결제기록 조회
     * @return 모든 결제기록 목록
     */
    public List<Purchase> findAll() {
        return em.createQuery("SELECT p FROM Purchase p", Purchase.class)
                .getResultList();
    }

    /**
     * 날짜와 날짜 사이의 매출을 찾음
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 구매 기록 목록
     */
    public List<Purchase> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery("SELECT p FROM Purchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate", Purchase.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

}
