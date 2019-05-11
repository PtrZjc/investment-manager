package pl.zajacp.investmentmanager.products;

import lombok.Data;
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
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "finance_products")
public class FinanceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String bank;

    @NotNull
    @Past
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate openDate;

    @NotNull
    @Min(1)
    private BigDecimal value;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100")
    @Column(precision = 5, scale = 4)
    private BigDecimal interest;

    @Column(length = 1000)
    private String notes;
    private Boolean isActive;
    private LocalDate created;

    @OneToMany(mappedBy = "product")
    private List<Action> actions;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {
        created = LocalDate.now();
        isActive = true;
    }
}

