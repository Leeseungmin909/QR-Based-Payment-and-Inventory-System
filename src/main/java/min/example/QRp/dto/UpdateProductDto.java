package min.example.QRp.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductDto {
    private String name;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price; // domain 계층에서 null 값이 넘어오면 이전값을 그대로 가져오기 위해 Integer 사용

    @Min(value = 0, message = "수량은 0개 이상이여야 합니다.")
    private Integer quantity; // domain 계층에서 null 값이 넘어오면 이전값을 그대로 가져오기 위해 Integer 사용
}
