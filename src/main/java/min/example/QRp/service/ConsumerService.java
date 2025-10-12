package min.example.QRp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import min.example.QRp.domain.Product;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.PurchaseRequestDto;
import min.example.QRp.repository.ProductRepository;
import min.example.QRp.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsumerService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;

    /**
     * 단일 제품 검색
     * @param productId 검색할 제품 ID
     * @return 해당 ID 제품
     */
    public ProductResponseDto findProductById(int productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다." + productId));
        return new ProductResponseDto(product);
    }

    /**
     * 제품 구매 메소드
     * @param purchaseRequestDto 구매할 제품
     * @return purchaseItems 리스트에 제품이 추가됨
     */
    @Transactional
    public Purchase createPurchase(PurchaseRequestDto purchaseRequestDto){
        List<PurchaseRequestDto.OrderItemDto> orderItems = purchaseRequestDto.getOrderList();
        List<PurchaseItem> purchaseItems = new ArrayList<>();
        for(PurchaseRequestDto.OrderItemDto itemDto : orderItems){
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("상품 ID " + itemDto.getProductId() + "을(를) 찾을 수 없습니다."));

            PurchaseItem purchaseItem = PurchaseItem.createPurchaseItem(
                    product,
                    product.getPrice(),
                    itemDto.getQuantity()
            );
            purchaseItems.add(purchaseItem);
        }
        Purchase purchase = Purchase.createPurchase(purchaseItems.toArray(new PurchaseItem[0]));
        purchaseRepository.create(purchase);
        return purchase;
    }
}
