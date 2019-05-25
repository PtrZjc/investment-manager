package pl.zajacp.investmentmanager.actionmanagement;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class ActionDto {

    //TODO existing date validator

    @NotNull
    @Future
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actionDate;

    @NotNull
    @Min(1)
    private BigDecimal amount;

    @NotNull
    private Boolean isSingle;

    @NotNull
    private Boolean isNegative;

    private String notes;
}
