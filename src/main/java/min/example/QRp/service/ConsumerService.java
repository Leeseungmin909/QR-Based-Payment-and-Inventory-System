package min.example.QRp.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import min.example.QRp.domain.Product;
import min.example.QRp.domain.Purchase;
import min.example.QRp.domain.PurchaseItem;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.PurchaseRequestDto;
import min.example.QRp.dto.kakao.KakaoApproveResponseDto;
import min.example.QRp.dto.kakao.KakaoReadyResponseDto;
import min.example.QRp.repository.ProductRepository;
import min.example.QRp.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConsumerService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final RestTemplate restTemplate;

    // 카카오페이 API 호출 필수 정보 (Admin Key, URL 등)
    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakao.api-url}")
    private String kakaoApiUrl;

    @Value("${kakao.payment.host}")
    private String paymentHost;

    @Value("${kakao.payment.success-url}")
    private String successUrl;

    @Value("${kakao.payment.cancel-url}")
    private String cancelUrl;

    @Value("${kakao.payment.fail-url}")
    private String failUrl;

    private final String KAKAO_READY_URL = "/v1/payment/ready";
    private final String KAKAO_APPROVE_URL = "/v1/payment/approve";
    private final String TEST_CID = "TC0ONETIME";


    /**
     * 장바구니 정보로 카카오페이 결제 준비 API를 호출
     * @param cart 장바구니 정보 <상품 ID, 구매수량>
     * @param session 현재 사용자 세션
     * @return 결제 페이지 리다이렉트 URL
     */
    public KakaoReadyResponseDto kakaoPayReady(Map<Integer, Integer> cart, HttpSession session) {

        //  장바구니 정보 조회 및 총액 계산
        Map<ProductResponseDto, Integer> detailedCart = getDetailedCart(cart);
        int totalAmount = detailedCart.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();

        // 상품명 생성
        String itemName = detailedCart.keySet().stream().findFirst().get().getName();
        if (detailedCart.size() > 1) {
            itemName += " 외 " + (detailedCart.size() - 1) + "건";
        }

        // 주문 ID, 유저 ID 생성
        String partnerOrderId = UUID.randomUUID().toString();
        String partnerUserId = session.getId();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoAdminKey);
        headers.set("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        // HTTP 바디 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", TEST_CID);
        body.add("partner_order_id", partnerOrderId);
        body.add("partner_user_id", partnerUserId);
        body.add("item_name", itemName);
        body.add("quantity", String.valueOf(cart.values().stream().mapToInt(Integer::intValue).sum()));
        body.add("total_amount", String.valueOf(totalAmount));
        body.add("tax_free_amount", "0");
        body.add("approval_url", paymentHost + successUrl);
        body.add("cancel_url", paymentHost + cancelUrl);
        body.add("fail_url", paymentHost + failUrl);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        KakaoReadyResponseDto responseDto = restTemplate.postForObject(
                kakaoApiUrl + KAKAO_READY_URL,
                entity,
                KakaoReadyResponseDto.class
        );

        // 세션에 정보 저장
        session.setAttribute("kakao_tid", responseDto.getTid());
        session.setAttribute("kakao_order_id", partnerOrderId);

        return responseDto;
    }

    /**
     * 카카오페이 "결제 승인" API를 호출후 결제 성공시 DB에 주문 저장
     * @param pgToken 결제 승인 토큰
     * @param session 결제 준비 단계에서 저장할 TID와 주문 정보를 불러올 세션
     * @return 결제 승인시간, 카드 정보등
     */
    @Transactional
    public KakaoApproveResponseDto kakaoPayApprove(String pgToken, HttpSession session) {

        // 세션에서 정보 꺼내기
        String tid = (String) session.getAttribute("kakao_tid");
        String partnerOrderId = (String) session.getAttribute("kakao_order_id");
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        String partnerUserId = session.getId();

        if (tid == null || partnerOrderId == null || cart == null) {
            throw new IllegalStateException("카카오페이 결제 정보가 세션에 없습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoAdminKey);
        headers.set("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("cid", TEST_CID);
        body.add("tid", tid);
        body.add("partner_order_id", partnerOrderId);
        body.add("partner_user_id", partnerUserId);
        body.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        // RestTemplate으로 카카오 결제 승인 API 호출
        KakaoApproveResponseDto responseDto = restTemplate.postForObject(
                kakaoApiUrl + KAKAO_APPROVE_URL,
                entity,
                KakaoApproveResponseDto.class
        );

        if (responseDto != null) {
            createPurchaseFromCart(cart);
            session.removeAttribute("kakao_tid");
            session.removeAttribute("kakao_order_id");
            session.removeAttribute("cart");
        }

        return responseDto;
    }

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

    /**
     * 장바구니안에 담겨있는 상품ID 및 수량을봄
     * @param cart <상품ID, 구매수량>
     * @return 상품 정보와 수량이 매핑된 결과 맵
     */
    public Map<ProductResponseDto, Integer> getDetailedCart(Map<Integer, Integer> cart) {
        Map<ProductResponseDto, Integer> detailedCart = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 상품 ID " + productId + "을(를) 찾을 수 없습니다."));

            detailedCart.put(new ProductResponseDto(product), quantity);
        }
        return detailedCart;
    }

    /**
     * 현재 장바구니가 재고를 초과하는지 검증
     * @param cart <상품ID, 구매수량>
     */
    public void validateStock(Map<Integer, Integer> cart) {
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new EntityNotFoundException("상품 ID " + entry.getKey() + "을(를) 찾을 수 없습니다."));

            if (product.getQuantity() < entry.getValue()) {
                throw new IllegalStateException(product.getName() + "의 재고가 부족합니다. (남은 수량: " + product.getQuantity() + "개)");
            }
        }
    }

    /**
     * 장바구니 안에 있는 제품 구매로직 실행
     * @param cart <상품ID, 상품수량>
     * @return DB에 저장된 최종 Purchase 엔티티
     */
    @Transactional
    public Purchase createPurchaseFromCart(Map<Integer, Integer> cart) {
        validateStock(cart);
        PurchaseRequestDto dto = new PurchaseRequestDto();
        List<PurchaseRequestDto.OrderItemDto> orderList = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            PurchaseRequestDto.OrderItemDto itemDto = new PurchaseRequestDto.OrderItemDto();
            itemDto.setProductId(entry.getKey());
            itemDto.setQuantity(entry.getValue());
            orderList.add(itemDto);
        }
        dto.setOrderList(orderList);
        return createPurchase(dto);
    }
}
