package pl.zajacp.investmentmanager.actionmanagement;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import pl.zajacp.investmentmanager.products.FinanceProduct;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actionDate;

    private BigDecimal balanceChange;
    private BigDecimal afterActionValue;

    @Column(length = 1000)
    private String notes;
    private Boolean isDone;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private FinanceProduct product;

    //validatory: actionDate

    @PrePersist
    public void prePersist() {
        isDone = false;
    }

    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", actionType=" + actionType +
                ", actionDate=" + actionDate +
                ", balanceChange=" + balanceChange +
                ", afterActionValue=" + afterActionValue +
                ", notes='" + notes + '\'' +
                ", isDone=" + isDone +
                '}';
    }
}


/*
 ***  deposit/widthdraw

 actionDate
 balanceChange


 ***  capitalization

 actionDate
 afterActionValue
 isDone

 ***  product open

 actionDate
 value

 ***  product close

 actionDate
 value
 */


