package min.example.QRp.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import min.example.QRp.dto.ProductResponseDto;
import min.example.QRp.dto.kakao.KakaoApproveResponseDto;
import min.example.QRp.dto.kakao.KakaoReadyResponseDto;
import min.example.QRp.service.ConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final ConsumerService consumerService;

    /**
     * 세션에 저장된 장바구니 목록을 화면에 보여줌
     * @param session 장바구니 데이터를 조회하고 관리하기 위한 세션
     * @param model HTML에 전달할 장바구니
     * @return 장바구니 화면 뷰
     */
    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {

        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            model.addAttribute("cartItems", new ArrayList<>());
            model.addAttribute("totalAmount", 0);
        } else {
            try {
                Map<ProductResponseDto, Integer> detailedCart = consumerService.getDetailedCart(cart);

                int totalAmount = detailedCart.entrySet().stream()
                        .mapToInt(entry -> entry.getKey().getPrice() * entry.getValue())
                        .sum();

                model.addAttribute("cartItems", detailedCart.entrySet());
                model.addAttribute("totalAmount", totalAmount);

            } catch (EntityNotFoundException ex) {
                session.removeAttribute("cart");
                model.addAttribute("cartItems", new ArrayList<>());
                model.addAttribute("totalAmount", 0);
                model.addAttribute("errorMessage", "장바구니에 담긴 상품 중 일부가 삭제되었습니다.");
            }
        }

        return "consumer/cart";
    }

    /**
     * '+' 버튼 클릭시 그 제품 1개추가후 재고 유효성 검사
     * @param productId 제품 증가시킬 제품ID
     * @param session 장바구니 데이터 세션
     * @param rttr 재고 부족시 에러 메세지를 보낼 객체
     * @return 장바구니 페이지 리다이렉트
     */
    @GetMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable int productId, HttpSession session, RedirectAttributes rttr) {

        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }

        // 수량을 1 증가시킴
        cart.put(productId, cart.getOrDefault(productId, 0) + 1);
        session.setAttribute("cart", cart);

        // 재고 검증 재고가 초과되면 다시 재고 -1로 감소시킴
        try {
            consumerService.validateStock(cart);
        } catch (IllegalStateException ex) {
            cart.put(productId, cart.get(productId) - 1);
            session.setAttribute("cart", cart);
            rttr.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/consumer/cart";
    }

    /**
     * '-' 버튼 클릭시 그 제품 1개 감소시키거나 0개가 될 경우 장바구니에서 삭제
     * @param productId 제품 감소시킬 제품ID
     * @param session 장바구니 데이터 세션
     * @return 장바구니 페이지 리다이렉트
     */
    @GetMapping("/cart/subtract/{productId}")
    public String subtractFromCart(@PathVariable int productId, HttpSession session) {

        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            return "redirect:/consumer/cart";
        }

        int currentQuantity = cart.getOrDefault(productId, 0);

        if (currentQuantity > 1) {
            // 수량이 2 이상이면 1만 뺌
            cart.put(productId, currentQuantity - 1);
        } else {
            // 수량이 1 이하면 상품 자체를 장바구니에서 제거
            cart.remove(productId);
        }

        session.setAttribute("cart", cart);
        return "redirect:/consumer/cart";
    }

    /**
     * 'X' 버튼 클릭시 그 상품을 장바구니에서 아예 삭제
     * @param productId 제거할 제품ID
     * @param session 장바구니 데이터 세션
     * @return 장바구니 페이지 리다이렉트
     */
    @GetMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable int productId, HttpSession session) {

        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(productId);
            session.setAttribute("cart", cart);
        }

        return "redirect:/consumer/cart";
    }

    /**
     * 카카오 결제 준비 API
     * @param session 결제할 장바구니 정보 조회 및 승인 단계에서 사용할 TID 저장 세션
     * @return 성공시 모바일 결제 URL을 포함한 ResponseEntity, 실패시 에러 메세지 반환
     */
    @PostMapping("/payment/ready")
    @ResponseBody
    public ResponseEntity<?> paymentReady(HttpSession session) {

        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            Map<String, String> error = Map.of("error", "장바구니가 비어있습니다.");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            KakaoReadyResponseDto readyResponse = consumerService.kakaoPayReady(cart, session);

            Map<String, String> response = new HashMap<>();
            response.put("next_redirect_mobile_url", readyResponse.getNextRedirectMobileUrl());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            throw new IllegalStateException("결제 준비 중 오류 발생: " + ex.getMessage());
        }
    }

    /**
     * 카카오 결제 승인 API
     * @param pgToken 카카오페이 서버로부터 받은 결제 승인 토큰
     * @param session 결제 정보(TID, 제품ID)가 저장된 세션
     * @param model 결제 승인 결과 및 실패시 에러 메세지를 보여줄 모델
     * @return 성공시 결제 성공창 실패시 결제 실패창
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("pg_token") String pgToken, HttpSession session, Model model) {

        try {
            KakaoApproveResponseDto approveResponse = consumerService.kakaoPayApprove(pgToken, session);
            model.addAttribute("approveResponse", approveResponse);
            return "consumer/payment-success";

        } catch (Exception ex) {
            model.addAttribute("errorMessage", "결제 승인 중 오류가 발생했습니다: " + ex.getMessage());
            return "consumer/payment-fail";
        }
    }

    /**
     * 결제중 사용자가 취소 했을떄 보여지는 페이지
     * @return 결제 취소 안내 화면
     */
    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "consumer/payment-cancel";
    }

    /**
     * 결제 진행중 시간초과 및 시스템 오류등 실패했을떄 보여지는 페이지
     * @return 결제 실패 안내 화면
     */
    @GetMapping("/payment/fail")
    public String paymentFail() {
        return "consumer/payment-fail";
    }
}