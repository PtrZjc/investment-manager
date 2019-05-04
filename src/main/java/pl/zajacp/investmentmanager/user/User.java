package pl.zajacp.investmentmanager.user;

import lombok.Data;
import pl.zajacp.investmentmanager.investments.FinanceProduct;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=40)
    private String name;
    private String password;
    private String email;

    private String role;

    @OneToMany(mappedBy = "user")
    private List<FinanceProduct> products = new ArrayList<>();

}
