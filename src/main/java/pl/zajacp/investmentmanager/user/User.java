package pl.zajacp.investmentmanager.user;

import lombok.Data;
import pl.zajacp.investmentmanager.products.FinanceProduct;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String login;
    private String password;
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<FinanceProduct> products = new HashSet<>();

    public void addFinanceProduct(FinanceProduct product) {
        products.add(product);
    }
}
