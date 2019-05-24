package pl.zajacp.investmentmanager.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;
import pl.zajacp.investmentmanager.user.UserService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ActionService actionService;
    private final UserService userService;

    @Autowired
    public ProductController(ProductService productService, ActionService actionService, UserService userService) {
        this.productService = productService;
        this.actionService = actionService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String homePage() {
        return "redirect:/product/add";
    }

    @GetMapping("/add")
    public String chooseProduct() {
        return "productFormChoose";
    }

    @GetMapping("/add/investment")
    public String addInvestment(Investment product, Model model) {
        model.addAttribute("product", new Investment());
        return "productFormInvestment";
    }

    @PostMapping("/add/investment")
    public String postInvestment(@ModelAttribute("product") @Valid Investment product, BindingResult result) {
        if (result.hasErrors()) {
            return "productFormInvestment";
        }

//        actionService.generateInvestmentActions(product);
        productService.save(product);

        return "success";
    }

    @GetMapping("/add/savings-account")
    public String addSavingsAccount(SavingsAccount product, Model model) {
        model.addAttribute("product", new SavingsAccount());
        return "productFormSavingsAccount";
    }

    @PostMapping("/add/savings-account")
    public String postSavingsAccount(@ModelAttribute("product") @Valid SavingsAccount product, BindingResult result) {
        if (result.hasErrors()) {
            return "productFormSavingsAccount";
        }
        productService.save(product);
        return "success";
    }

    @GetMapping("/all")
    public String showAllProducts(Model model) {
        Map<Class, List<? extends FinanceProduct>> products = productService.findAllOfLoggedUser();
        model.addAttribute("investments", products.get(Investment.class));
        model.addAttribute("savingsAccounts", products.get(SavingsAccount.class));
        return "showAllProducts";
    }

    @PostMapping("/edit")
    public String editProduct(@RequestParam(name = "id") Long id, Model model) {

        FinanceProduct product = productService.findById(id);
        model.addAttribute("product", product);

        if (product instanceof Investment) {
            return "productFormInvestment";
        } else if (product instanceof SavingsAccount) {
            return "productFormSavingsAccount";
        }
        return "/";
    }

    @PostMapping("/details")
    public String detailedProduct(@RequestParam(name = "id") Long id, Model model, HttpSession session) {

        session.setAttribute("productId",id);

        FinanceProduct product = productService.findById(id);

        productService.sortActionsByDate(product, false);
        model.addAttribute("product", product);

        if (product instanceof Investment) {
            return "productDetailsInvestment";
        } else if (product instanceof SavingsAccount) {
            return "productDetailsSavingsAccount";
        }
        return "/";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam(name = "id") Long id) {
        productService.deleteById(id);
        return "redirect:all";
    }

}
