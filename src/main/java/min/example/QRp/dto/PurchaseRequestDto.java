package min.example.QRp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseRequestDto {
    @NotEmpty
    private List<OrderItemDto> orderList;

    @Getter
    @Setter
    public static class OrderItemDto{
        @NotNull
        private int productId;

        @Min(1)
        private int quantity;
    }
}
