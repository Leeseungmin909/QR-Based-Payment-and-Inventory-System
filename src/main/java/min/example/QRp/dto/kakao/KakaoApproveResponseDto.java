package min.example.QRp.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// 카카오 "결제 승인" API가 님에게 보내는 최종 응답 DTO
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoApproveResponseDto {

    @JsonProperty("aid")
    private String aid; // 결제 승인 번호

    @JsonProperty("tid")
    private String tid; // 결제 고유 번호

    @JsonProperty("payment_method_type")
    private String paymentMethodType; // CARD 또는 MONEY

    @JsonProperty("item_name")
    private String itemName; // 상품명
}