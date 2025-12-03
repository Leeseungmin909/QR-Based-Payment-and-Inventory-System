package min.example.QRp.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

// 카카오 "결제 승인" API에 보낼 요청 DTO
@Data
@Builder
public class KakaoApproveRequestDto {
    @JsonProperty("cid")
    private String cid; // 가맹점 코드

    @JsonProperty("tid")
    private String tid; // "결제 준비"에서 받았던 tid

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // "결제 준비"에서 보냈던 주문 ID

    @JsonProperty("partner_user_id")
    private String partnerUserId; // "결제 준비"에서 보냈던 유저 ID

    @JsonProperty("pg_token")
    private String pgToken; //  결제 성공 시 카카오가 보내주는 pg_token
}