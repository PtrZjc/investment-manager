package pl.zajacp.investmentmanager.actionmanagement;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import pl.zajacp.investmentmanager.actionmanagement.validation.SingleWidthdraw;
import pl.zajacp.investmentmanager.actionmanagement.validation.ThisMonthAndLater;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SingleWidthdraw
public class ActionDto {


    //TODO Validate: After open, before close
    @NotNull
    @ThisMonthAndLater
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
