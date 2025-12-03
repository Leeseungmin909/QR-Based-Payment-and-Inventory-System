package min.example.QRp.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import min.example.QRp.domain.Product;
import min.example.QRp.dto.CreateProductDto;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.PurchaseResponseDto;
import min.example.QRp.dto.UpdateProductDto;
import min.example.QRp.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    /**
     * 모든 상품을 보여줌
     * @param model View에 데이터를 전달하기 위한 객체
     * @return 렌더링할 html 파일
     */
    @GetMapping("/products")
    public String findAllProducts(Model model){
        List<ProductResponseDto> products = adminService.findAllProducts();
        model.addAttribute("products",products);
        return "admin/products";
    }

    /**
     * 제품 이름을 입력해서 상품을 찾음
     * @param model View에 데이터를 전달하기 위한 객체
     * @param name 사용자에게 입력받은 이름
     * @return 렌더링할 html 파일
     */
    @GetMapping("/products/search")
    public String findByNameProduct(Model model, @RequestParam("name") String name, RedirectAttributes rttr){
        try {
            ProductResponseDto product = adminService.findProductByName(name);
            model.addAttribute("product", product);
            return "admin/product-detail";

        } catch (EntityNotFoundException ex) {
            rttr.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/products";
        }
    }

    /**
     * 제품 생성
     * @param createProductDto 사용자에게 입력받은 제품 이름,가격,수량
     * @return 제품 생성
     */
    @PostMapping("/products")
    @ResponseBody
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody CreateProductDto createProductDto){
        Product newProduct = adminService.createProduct(createProductDto);
        ProductResponseDto responseDto = new ProductResponseDto(newProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 제품 수정
     * @param productId 수정할 제품 id
     * @param updateProductDto 수정할 제품 이름,가격,수량 (변경하고 싶지 않다면 입력란 비워두기(null))
     * @return 제품 수정
     */
    @PatchMapping("/products/{productId}")
    @ResponseBody
    public  ResponseEntity<ProductResponseDto> updateProduct(@PathVariable int productId, @Valid @RequestBody UpdateProductDto updateProductDto){
        Product updateProduct = adminService.updateProduct(productId,updateProductDto);
        ProductResponseDto responseDto = new ProductResponseDto(updateProduct);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 단일 제품 검색
     * @param productId 조회할 제품 id
     * @return 제품 정보
     */
    @GetMapping("/products/{productId}")
    @ResponseBody
    public ResponseEntity<ProductResponseDto> findProductById(@PathVariable int productId) {
        ProductResponseDto product = adminService.findProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * 제품 삭제
     * @param productId 삭제할 제품 id
     * @return 제품 삭제
     */
    @DeleteMapping("/products/{productId}")
    @ResponseBody
    public ResponseEntity<String> deleteProduct(@PathVariable int productId){
        adminService.deleteProduct(productId);
        return ResponseEntity.ok(productId + "번 상품이 삭제되었습니다.");
    }

    /**
     * 환불 기능
     * state를 REFUNDED 변경후 구매된 제품수량을 다시 더해준다.
     * @param purchaseId 환불할 구매 id
     * @return 제품 환불
     */
    @PostMapping("/purchases/{purchaseId}/refund")
    @ResponseBody
    public ResponseEntity<String> refundPurchases(@PathVariable int purchaseId){
        adminService.refundPurchase(purchaseId);
        return ResponseEntity.ok(purchaseId + "번 상품이 환불되었습니다.");
    }

    /**
     * 주문 ID를 입력해서 주문을 찾음
     * @param model View에 데이터를 전달하기 위한 객체
     * @param purchaseId 사용자에게 입력받은 주문 ID
     * @param rttr 리다이렉트 시 메시지를 전달하기 위한 객체
     * @return 렌더링할 html 파일
     */
    @GetMapping("/purchases/search")
    public String findByPurchaseId(Model model, @RequestParam("purchaseId") int purchaseId, RedirectAttributes rttr) {
        try {
            PurchaseResponseDto purchase = adminService.findPurchaseById(purchaseId);
            model.addAttribute("purchase", purchase);
            return "admin/purchase-detail";

        } catch (EntityNotFoundException ex) {
            rttr.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/purchases";
        }
    }

    /**
     * 제품을 만들때 DB에 QR.코드를 저장하지않고 (QR.코드 보기) 버튼등을 눌렀을떄 생성,볼수있는 메소드
     * @param productId QR.코드 생성,읽을 제품 ID
     * @return QR.코드 생성,읽기
     */
    @GetMapping(value = "/products/{productId}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable int productId) {
        try {
            byte[] qrCodeImage = adminService.generateQrCodeImage(productId);
            return ResponseEntity.ok().body(qrCodeImage);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 모든 주문 내역을 보여줌
     * @param model View에 데이터를 전달하기 위한 객체
     * @return 렌더링할 html 파일
     */
    @GetMapping("/purchases")
    public String findAllPurchases(Model model) {
        List<PurchaseResponseDto> purchases = adminService.findAllPurchases();
        model.addAttribute("purchases", purchases);
        return "admin/purchases";
    }
}
