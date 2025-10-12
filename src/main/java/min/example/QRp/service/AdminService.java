package min.example.QRp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import min.example.QRp.domain.Product;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import min.example.QRp.domain.PurchaseState;
import min.example.QRp.dto.CreateProductDto;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.UpdateProductDto;
import min.example.QRp.repository.ProductRepository;
import min.example.QRp.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;


    /**
     * 제품을 생성하는 메소드
     * @param createProductDto 생성할 제품 이름,가격,수량
     * @return db에 제품 저장
     */
    @Transactional
    public Product createProduct(CreateProductDto createProductDto){
        productRepository.findByName(createProductDto.getName()).ifPresent(product -> {throw new IllegalStateException("이미 존재하는 상품 이름입니다.");});

        Product newProduct = Product.builder()
                .name(createProductDto.getName())
                .price(createProductDto.getPrice())
                .quantity(createProductDto.getQuantity())
                .build();

        return productRepository.create(newProduct);
    }

    /**
     * 단일 제품 검색(ID)
     * @param productId 검색할 제품 ID
     * @return 해당 ID 제품
     */
    public ProductResponseDto findProductById(int productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다." + productId));
        return new ProductResponseDto(product);
    }

    /**
     * 단일 제품 검색(NAME)
     * @param name 검색할 제품 이름
     * @return 해당 제품 이름
     */
    public ProductResponseDto findProductByName(String name){
        Product product = productRepository.findByName(name)
                .orElseThrow(()-> new EntityNotFoundException("해당 이름의 상품을 찾을 수 없습니다." + name));
        return new ProductResponseDto(product);
    }

    /**
     * 모든 제품 검색
     * @return 모든 제품
     */
    public List<ProductResponseDto> findAllProducts(){
        List<Product> productList = productRepository.findAll();
        return productList.stream()
                .map(product -> new ProductResponseDto(product))
                .collect(Collectors.toList());
    }

    /**
     * 제품 수정
     * @param productid 수정할 제품 id
     * @param updateProductDto 수정할 제품(이름,가격,수량)
     *                         수정을 원치 않을경우 빈칸(null)으로 제출
     * @return 제품 수정
     */
    @Transactional
    public Product updateProduct(int productid, UpdateProductDto updateProductDto){
        Product product = productRepository.findById(productid)
                        .orElseThrow(() -> new EntityNotFoundException("해당 ID의 제품을 찾을 수 없습니다." + productid));

        product.update(
                updateProductDto.getName(),
                updateProductDto.getPrice(),
                updateProductDto.getQuantity()
        );
        return product;
    }

    /**
     * 제품 삭제
     * @param productId 삭제할 제품 id
     */
    @Transactional
    public void deleteProduct(int productId){
        productRepository.delete(productId);
    }

    /**
     * 제품 환불 메소드
     * @param purchaseId 환불할 구매 번호
     */
    @Transactional
    public void refundPurchase(int purchaseId){
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID를 찾을 수 없습니다."+ purchaseId));

        if (purchase.getState() == PurchaseState.REFUNDED){
            throw new IllegalStateException("이미 환불 처리가 되었습니다.");
        }
        purchase.changeState(PurchaseState.REFUNDED);

        for (PurchaseItem item : purchase.getPurchaseItems()){
            item.getProduct().addStock(item.getOrderQuantity());
        }
    }

    /**
     * QR 코드 생성
     * @param productId 제품 ID
     * @return QR 코드
     * @throws Exception QR 코드 이미지를 스트림에 쓰는 데 실패할 경우
     */
    public byte[] generateQrCodeImage(int productId) throws Exception {
        // QR 코드에 담을 상품 ID
        String content = String.valueOf(productId);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // QR 코드 크기 및 내용 설정
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

        // QR 코드를 PNG 이미지 데이터로 변환
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return pngOutputStream.toByteArray();
    }

}
