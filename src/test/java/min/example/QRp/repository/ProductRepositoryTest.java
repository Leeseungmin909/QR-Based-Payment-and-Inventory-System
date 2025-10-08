package min.example.QRp.repository;

import min.example.QRp.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 완료 후 데이터를 롤백하여 다음 테스트에 영향을 주지 않도록 함
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @DisplayName("제품을 저장하고 ID로 조회하면 해당 제품이 조회되어야 한다.")
    @Test
    void createAndFindById() {
        // given
        Product product = new Product("test-product", 10000, 10);

        // when
        Product savedProduct = productRepository.create(product);
        Optional<Product> foundProductOptional = productRepository.findById(savedProduct.getProductId());

        // then
        assertThat(foundProductOptional).isPresent(); // Optional이 비어있지 않은지 확인
        Product foundProduct = foundProductOptional.get();
        assertThat(foundProduct.getProductId()).isEqualTo(savedProduct.getProductId());
        assertThat(foundProduct.getName()).isEqualTo("test-product");
    }

    @DisplayName("제품 이름으로 조회하면 해당 제품이 조회되어야 한다.")
    @Test
    void findByName() {
        // given
        Product product1 = new Product("Apple Mouse", 50000, 5);
        productRepository.create(product1);

        // when
        Optional<Product> foundProductOptional = productRepository.findByName("Apple Mouse");

        // then
        assertThat(foundProductOptional).isPresent();
        assertThat(foundProductOptional.get().getName()).isEqualTo("Apple Mouse");
    }

    @DisplayName("존재하지 않는 이름으로 조회하면 비어있는 Optional 객체가 반환되어야 한다.")
    @Test
    void findByName_notFound() {
        // given
        // 아무것도 저장하지 않음

        // when
        Optional<Product> foundProductOptional = productRepository.findByName("NonExistentProduct");

        // then
        assertThat(foundProductOptional).isEmpty();
    }

    @DisplayName("모든 제품을 조회하면 저장된 모든 제품 목록이 반환되어야 한다.")
    @Test
    void findAll() {
        // given
        Product product1 = new Product("product-A", 100, 1);
        Product product2 = new Product("product-B", 200, 2);
        productRepository.create(product1);
        productRepository.create(product2);

        // when
        List<Product> products = productRepository.findAll();

        // then
        assertThat(products).hasSize(2);
        assertThat(products).extracting("name").containsExactlyInAnyOrder("product-A", "product-B");
    }

    @DisplayName("제품 정보를 변경하면 DB에 반영되어야 한다.")
    @Test
    void update() {
        // given
        Product originalProduct = new Product("original-name", 1000, 10);
        productRepository.create(originalProduct);

        // when
        // 도메인 객체의 update 메서드를 사용하여 상태 변경
        originalProduct.update("updated-name", 2000, 20);
        // Repository의 update는 사용하지 않아도 @Transactional에 의해 변경 감지(dirty checking) 되어 DB에 반영됨
        // em.merge()를 사용하는 명시적인 update 메서드를 테스트하고 싶다면 아래와 같이 호출
        // productRepository.update(originalProduct);

        // then
        Optional<Product> foundProductOptional = productRepository.findById(originalProduct.getProductId());
        assertThat(foundProductOptional).isPresent();
        Product foundProduct = foundProductOptional.get();
        assertThat(foundProduct.getName()).isEqualTo("updated-name");
        assertThat(foundProduct.getPrice()).isEqualTo(2000);
    }

    @DisplayName("제품을 삭제하면 DB에서 조회되지 않아야 한다.")
    @Test
    void delete() {
        // given
        Product product = new Product("product-to-delete", 999, 9);
        productRepository.create(product);
        int productId = product.getProductId();

        // when
        productRepository.delete(productId);

        // then
        Optional<Product> foundProductOptional = productRepository.findById(productId);
        assertThat(foundProductOptional).isEmpty();
    }
}