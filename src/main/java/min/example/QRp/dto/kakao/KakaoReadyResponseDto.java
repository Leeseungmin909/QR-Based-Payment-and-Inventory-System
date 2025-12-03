package min.example.QRp.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// 카카오 결제 준비 API 응답 DTO
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 필요 없는 필드는 무시
public class KakaoReadyResponseDto {

    @JsonProperty("tid")
    private String tid; // 결제 고유 번호

    @JsonProperty("next_redirect_mobile_url")
    private String nextRedirectMobileUrl; //  폰에서 카톡 앱을 여는 URL

}