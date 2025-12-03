package min.example.QRp.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

// 카카오 "결제 준비" API에 보낼 요청 DTO
@Data
@Builder
public class KakaoReadyRequestDto {
    @JsonProperty("cid")
    private String cid; // 가맹점 코드

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 서버 주문 ID

    @JsonProperty("partner_user_id")
    private String partnerUserId; // 서버 유저 ID

    @JsonProperty("item_name")
    private String itemName; // 상품명

    @JsonProperty("quantity")
    private Integer quantity; // 총 수량

    @JsonProperty("total_amount")
    private Integer totalAmount; // 총 금액

    @JsonProperty("tax_free_amount")
    private Integer taxFreeAmount; // 비과세 금액


    // 결제 완료, 취소, 실패 시 카카오가 리다이렉트할 서버 URL
    @JsonProperty("approval_url")
    private String approvalUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    @JsonProperty("fail_url")
    private String failUrl;
}