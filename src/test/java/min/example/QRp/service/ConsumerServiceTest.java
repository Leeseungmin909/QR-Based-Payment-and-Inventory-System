package min.example.QRp.service;

import jakarta.persistence.EntityNotFoundException;
import min.example.QRp.domain.Product;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.PurchaseRequestDto;
import min.example.QRp.repository.ProductRepository;
import min.example.QRp.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private ConsumerService consumerService;

    private Product testProduct;
    private PurchaseRequestDto purchaseRequestDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("테스트상품")
                .price(1000)
                .quantity(10)
                .build();

        // PurchaseRequestDto 생성
        purchaseRequestDto = new PurchaseRequestDto();
        List<PurchaseRequestDto.OrderItemDto> orderList = new ArrayList<>();

        PurchaseRequestDto.OrderItemDto orderItem = new PurchaseRequestDto.OrderItemDto();
        orderItem.setProductId(1);
        orderItem.setQuantity(2);

        orderList.add(orderItem);
        purchaseRequestDto.setOrderList(orderList);
    }

    @Test
    void findProductById_정상조회() {
        // given
        int productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // when
        ProductResponseDto result = consumerService.findProductById(productId);

        // then
        assertNotNull(result);
        assertEquals(testProduct.getName(), result.getName());
        assertEquals(testProduct.getPrice(), result.getPrice());
        assertEquals(testProduct.getQuantity(), result.getQuantity());
        verify(productRepository).findById(productId);
    }

    @Test
    void findProductById_존재하지않는상품_예외발생() {
        // given
        int productId = 999;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> consumerService.findProductById(productId));

        assertTrue(exception.getMessage().contains("해당 ID의 상품을 찾을 수 없습니다."));
        verify(productRepository).findById(productId);
    }


    @Test
    void createPurchase_단일상품구매() {
        // given
        Product product = Product.builder()
                .name("상품1")
                .price(1000)
                .quantity(10)
                .build();

        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(purchaseRepository.create(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Purchase result = consumerService.createPurchase(purchaseRequestDto);

        // then
        assertNotNull(result);
        assertEquals(1, result.getPurchaseItems().size());
        assertEquals(8, product.getQuantity()); // 10 - 2 = 8
        verify(productRepository).findById(1);
        verify(purchaseRepository).create(any(Purchase.class));
    }

    @Test
    void createPurchase_여러상품구매() {
        // given
        Product product1 = Product.builder().name("상품1").price(1000).quantity(10).build();
        Product product2 = Product.builder().name("상품2").price(2000).quantity(15).build();

        PurchaseRequestDto multiOrderDto = new PurchaseRequestDto();
        List<PurchaseRequestDto.OrderItemDto> orderList = new ArrayList<>();

        PurchaseRequestDto.OrderItemDto order1 = new PurchaseRequestDto.OrderItemDto();
        order1.setProductId(1);
        order1.setQuantity(2);

        PurchaseRequestDto.OrderItemDto order2 = new PurchaseRequestDto.OrderItemDto();
        order2.setProductId(2);
        order2.setQuantity(3);

        orderList.add(order1);
        orderList.add(order2);
        multiOrderDto.setOrderList(orderList);

        when(productRepository.findById(1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2)).thenReturn(Optional.of(product2));
        when(purchaseRepository.create(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Purchase result = consumerService.createPurchase(multiOrderDto);

        // then
        assertNotNull(result);
        assertEquals(2, result.getPurchaseItems().size());
        assertEquals(8, product1.getQuantity()); // 10 - 2 = 8
        assertEquals(12, product2.getQuantity()); // 15 - 3 = 12
        verify(productRepository).findById(1);
        verify(productRepository).findById(2);
        verify(purchaseRepository).create(any(Purchase.class));
    }

    @Test
    void createPurchase_존재하지않는상품_예외발생() {
        // given
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> consumerService.createPurchase(purchaseRequestDto));

        assertTrue(exception.getMessage().contains("상품 ID"));
        assertTrue(exception.getMessage().contains("을(를) 찾을 수 없습니다."));
        verify(purchaseRepository, never()).create(any(Purchase.class));
    }

    @Test
    void createPurchase_재고부족_예외발생() {
        // given
        Product lowStockProduct = Product.builder()
                .name("재고부족상품")
                .price(1000)
                .quantity(1)
                .build();

        PurchaseRequestDto.OrderItemDto orderItem = new PurchaseRequestDto.OrderItemDto();
        orderItem.setProductId(1);
        orderItem.setQuantity(5); // 재고보다 많은 수량 요청

        PurchaseRequestDto requestDto = new PurchaseRequestDto();
        requestDto.setOrderList(List.of(orderItem));

        when(productRepository.findById(1)).thenReturn(Optional.of(lowStockProduct));

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> consumerService.createPurchase(requestDto));

        assertTrue(exception.getMessage().contains("재고가 부족합니다."));
        verify(purchaseRepository, never()).create(any(Purchase.class));
    }





    @Test
    void createPurchase_빈주문목록() {
        // given
        PurchaseRequestDto emptyDto = new PurchaseRequestDto();
        emptyDto.setOrderList(new ArrayList<>());

        when(purchaseRepository.create(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Purchase result = consumerService.createPurchase(emptyDto);

        // then
        assertNotNull(result);
        assertTrue(result.getPurchaseItems().isEmpty());
        verify(purchaseRepository).create(any(Purchase.class));
    }
}