package pl.zajacp.investmentmanager.user;

import lombok.Data;
import pl.zajacp.investmentmanager.investments.FinanceProduct;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(length=40)
    @Size(min=5, max=20)
    private String name;
    private String password;
    @Email
    private String email;

    @OneToMany(mappedBy = "user")
    private List<FinanceProduct> products = new ArrayList<>();
}
