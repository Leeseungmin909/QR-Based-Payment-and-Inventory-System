package min.example.QRp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductDto {
    @NotBlank(message = "이름은 비워둘 수 없습니다.")
    private String name;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.") // controller 계층에서 @Valid 어노테이션을 사용해야함
    private int price;

    @Min(value = 0, message = "수량은 0개 이상이여야 합니다.")
    private int quantity;
}

