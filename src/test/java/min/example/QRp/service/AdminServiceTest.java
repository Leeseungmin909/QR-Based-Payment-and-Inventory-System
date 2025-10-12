package min.example.QRp.service;

import jakarta.persistence.EntityNotFoundException;
import min.example.QRp.domain.Product;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import min.example.QRp.domain.PurchaseState;
import min.example.QRp.dto.CreateProductDto;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.UpdateProductDto;
import min.example.QRp.repository.ProductRepository;
import min.example.QRp.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private AdminService adminService;

    private Product testProduct;
    private CreateProductDto createProductDto;
    private UpdateProductDto updateProductDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("테스트상품")
                .price(1000)
                .quantity(10)
                .build();

        createProductDto = new CreateProductDto();
        createProductDto.setName("새상품");
        createProductDto.setPrice(2000);
        createProductDto.setQuantity(5);

        updateProductDto = new UpdateProductDto();
        updateProductDto.setName("수정상품");
        updateProductDto.setPrice(1500);
        updateProductDto.setQuantity(8);
    }

    @Test
    void createProduct_정상생성() {
        // given
        when(productRepository.findByName(createProductDto.getName())).thenReturn(Optional.empty());
        when(productRepository.create(any(Product.class))).thenReturn(testProduct);

        // when
        Product result = adminService.createProduct(createProductDto);

        // then
        assertNotNull(result);
        verify(productRepository).findByName(createProductDto.getName());
        verify(productRepository).create(any(Product.class));
    }

    @Test
    void createProduct_중복상품명_예외발생() {
        // given
        when(productRepository.findByName(createProductDto.getName()))
                .thenReturn(Optional.of(testProduct));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminService.createProduct(createProductDto));

        assertEquals("이미 존재하는 상품 이름입니다.", exception.getMessage());
        verify(productRepository, never()).create(any(Product.class));
    }

    @Test
    void findProductById_정상조회() {
        // given
        int productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        ProductResponseDto result = adminService.findProductById(productId);

        // then
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void findProductById_존재하지않는상품_예외발생() {
        // given
        int productId = 999;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.findProductById(productId));

        assertTrue(exception.getMessage().contains("해당 ID의 상품을 찾을 수 없습니다."));
    }

    @Test
    void findAllProducts_정상조회() {
        // given
        Product product1 = Product.builder().name("상품1").price(1000).quantity(5).build();
        Product product2 = Product.builder().name("상품2").price(2000).quantity(10).build();
        List<Product> products = List.of(product1, product2);

        when(productRepository.findAll()).thenReturn(products);

        // when
        List<ProductResponseDto> result = adminService.findAllProducts();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void findAllProducts_빈목록() {
        // given
        when(productRepository.findAll()).thenReturn(List.of());

        // when
        List<ProductResponseDto> result = adminService.findAllProducts();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_정상수정() {
        // given
        int productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        Product result = adminService.updateProduct(productId, updateProductDto);

        // then
        assertNotNull(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void updateProduct_부분수정_이름만() {
        // given
        int productId = 1;
        UpdateProductDto partialUpdate = new UpdateProductDto();
        partialUpdate.setName("새이름");

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        Product result = adminService.updateProduct(productId, partialUpdate);

        // then
        assertNotNull(result);
        verify(productRepository).findById(productId);
    }

    @Test
    void updateProduct_존재하지않는상품_예외발생() {
        // given
        int productId = 999;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class,
                () -> adminService.updateProduct(productId, updateProductDto));
    }

    @Test
    void deleteProduct_정상삭제() {
        // given
        int productId = 1;
        doNothing().when(productRepository).delete(productId);

        // when
        adminService.deleteProduct(productId);

        // then
        verify(productRepository).delete(productId);
    }

    @Test
    void refundPurchase_정상환불() {
        // given
        int purchaseId = 1;

        Product product = Product.builder()
                .name("상품1")
                .price(1000)
                .quantity(5)
                .build();

        PurchaseItem item = PurchaseItem.createPurchaseItem(product, 1000, 2);
        Purchase purchase = Purchase.createPurchase(item);
        purchase.changeState(PurchaseState.COMPLETED);

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        int initialQuantity = product.getQuantity();

        // when
        adminService.refundPurchase(purchaseId);

        // then
        assertEquals(PurchaseState.REFUNDED, purchase.getState());
        assertEquals(initialQuantity + 2, product.getQuantity());
        verify(purchaseRepository).findById(purchaseId);
    }

    @Test
    void refundPurchase_여러상품환불() {
        // given
        int purchaseId = 1;

        Product product1 = Product.builder().name("상품1").price(1000).quantity(5).build();
        Product product2 = Product.builder().name("상품2").price(2000).quantity(10).build();

        PurchaseItem item1 = PurchaseItem.createPurchaseItem(product1, 1000, 2);
        PurchaseItem item2 = PurchaseItem.createPurchaseItem(product2, 2000, 3);
        Purchase purchase = Purchase.createPurchase(item1, item2);
        purchase.changeState(PurchaseState.COMPLETED);

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));

        int initialQuantity1 = product1.getQuantity();
        int initialQuantity2 = product2.getQuantity();

        // when
        adminService.refundPurchase(purchaseId);

        // then
        assertEquals(PurchaseState.REFUNDED, purchase.getState());
        assertEquals(initialQuantity1 + 2, product1.getQuantity());
        assertEquals(initialQuantity2 + 3, product2.getQuantity());
    }

    @Test
    void refundPurchase_이미환불됨_예외발생() {
        // given
        int purchaseId = 1;
        Purchase refundedPurchase = Purchase.createPurchase();
        refundedPurchase.changeState(PurchaseState.REFUNDED);

        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(refundedPurchase));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> adminService.refundPurchase(purchaseId));

        assertEquals("이미 환불 처리가 되었습니다.", exception.getMessage());
    }

    @Test
    void refundPurchase_존재하지않는구매_예외발생() {
        // given
        int purchaseId = 999;
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.refundPurchase(purchaseId));

        assertTrue(exception.getMessage().contains("해당 ID를 찾을 수 없습니다."));
    }

    @Test
    void generateQrCodeImage_정상생성() throws Exception {
        // given
        int productId = 1;

        // when
        byte[] qrCode = adminService.generateQrCodeImage(productId);

        // then
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
    }

    @Test
    void generateQrCodeImage_다른ID로생성() throws Exception {
        // given
        int productId = 12345;

        // when
        byte[] qrCode = adminService.generateQrCodeImage(productId);

        // then
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
    }
}