package min.example.QRp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductDto {
    private int productId; // pk , productid 값 변경방지를위한 lombok setter 사용금함
    @NotEmpty
    private String name;
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.") // contoller 계층에서 @Valid 어노테이션을 사용해야함
    private int price;
    @Min(value = 0, message = "수량은 0개 이상이여야 합니다.")
    private int quantity;
}
