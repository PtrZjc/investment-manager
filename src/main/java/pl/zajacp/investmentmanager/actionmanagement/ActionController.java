package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.zajacp.investmentmanager.products.ProductService;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;
import pl.zajacp.investmentmanager.user.UserService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/product/operation")
public class ActionController {

    private final ProductService productService;
    private final ActionService actionService;
    private final UserService userService;

    @Autowired
    public ActionController(ProductService productService, ActionService actionService, UserService userService) {
        this.productService = productService;
        this.actionService = actionService;
        this.userService = userService;
    }

    @GetMapping("/add")
    public String chooseProduct() {
        return "actionForm";
    }

}