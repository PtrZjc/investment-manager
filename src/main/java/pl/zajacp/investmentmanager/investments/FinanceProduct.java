package pl.zajacp.investmentmanager.investments;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type", discriminatorType = DiscriminatorType.STRING)
@Table(name="finance_products")
public class FinanceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String bank;
    @NotNull
    @Min(1)
    private BigDecimal value;
    @NotNull
    private LocalDate openDate;
    @NotNull
    @DecimalMin("0.0001")
    @DecimalMax("1")
    @Column(precision = 5, scale=4)
    private BigDecimal interest;
    @Column(length=1000)
    private String notes;

}
