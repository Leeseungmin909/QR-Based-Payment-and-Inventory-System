package min.example.QRp.repository;

import jakarta.persistence.EntityManager;
import min.example.QRp.domain.Product;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final EntityManager em;

    public ProductRepository(EntityManager em) {
        this.em = em;
    }

    /**
     * 제품 저장
     * @param product 저장할 제품
     * @return db 제품 저장
     */
    public Product create(Product product){
        em.persist(product);
        return product;
    }

    /**
     * 제품 이름 조회
     * @param name 조회할 이름
     * @return 입력한 이름 제품 조회
     */
    public Optional<Product> findByName(String name){
        List<Product> result = em.createQuery("select p from Product p where p.name=:name", Product.class)
                .setParameter("name",name)
                .getResultList();
        return result.stream().findAny();
    }

    /**
     * 제품 아이디 조회
     * @param id 조회할 기본키
     * @return 입력한 제품 기본키 조회
     */
    public Optional<Product> findById(int id){
        Product product = em.find(Product.class,id);
        return Optional.ofNullable(product);
    }

    /**
     * 모든 제품 조회
     * @return 모든 제품 목록
     */
    public List<Product> findAll(){
        return em.createQuery("select p from Product p",Product.class)
                .getResultList();
    }

    /**
     * 제품 변경
     * @param productUpdate 변경할 제품
     * @return 제품 변경
     */
    @Transactional
    public Product update(Product productUpdate){
        return em.merge(productUpdate);
    }

    /**
     * 제품 삭제
     * @param deleteId 삭제할 아이디
     */
    @Transactional
    public void delete(int deleteId){
        Product product = em.find(Product.class, deleteId);

        if(product != null){
            em.remove(product);
        }
    }
}
