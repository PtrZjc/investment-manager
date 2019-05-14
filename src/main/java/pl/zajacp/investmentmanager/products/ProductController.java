package pl.zajacp.investmentmanager.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import javax.validation.Valid;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ActionService actionService;

    @Autowired
    public ProductController(ProductService productService, ActionService actionService) {
        this.productService = productService;
        this.actionService = actionService;
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
}
