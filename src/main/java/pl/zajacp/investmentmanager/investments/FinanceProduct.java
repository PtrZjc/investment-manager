package pl.zajacp.investmentmanager.investments;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.user.User;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate openDate;
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100")
    @Column(precision = 5, scale=4)
    private BigDecimal interest;
    @Column(length=1000)
    private String notes;

    @OneToMany(mappedBy = "product")
    private List<Action> actions;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private User user;
}

