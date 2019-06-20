package pl.zajacp.investmentmanager.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.zajacp.investmentmanager.actionmanagement.ActionService;
import pl.zajacp.investmentmanager.user.UserService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
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
    public String postInvestment(@ModelAttribute("product") @Valid Investment product,
                                 BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return "productFormInvestment";
        }

//        actionService.generateInvestmentActions(product);
        productService.save(product);
        session.setAttribute("productId", product.getId());
        return "redirect:/product/details";
    }

    @GetMapping("/add/savings-account")
    public String addSavingsAccount(SavingsAccount product, Model model) {
        model.addAttribute("product", new SavingsAccount());
        return "productFormSavingsAccount";
    }

    @PostMapping("/add/savings-account")
    public String postSavingsAccount(@ModelAttribute("product") @Valid SavingsAccount product, BindingResult result,
                                     @RequestParam("lastValidMonth") String validityDate, HttpSession session) {
        YearMonth validityMonth = YearMonth.parse(validityDate);

        if (!YearMonth.from(LocalDate.now()).isBefore(validityMonth)) {
            result.rejectValue("validityDate", "error.form.validityDate");
        }
        if (result.hasErrors()) {
            return "productFormSavingsAccount";
        }
        product.setValidityDate(validityMonth.atEndOfMonth());
        productService.save(product);
        session.setAttribute("productId", product.getId());
        return "redirect:/product/details";
    }

    @GetMapping("/all")
    public String showAllProducts(Model model, HttpSession session) {
        Map<Class, List<FinanceProduct>> products = productService.findAllOfLoggedUser();
        model.addAttribute("investments", products.get(Investment.class));
        model.addAttribute("savingsAccounts", products.get(SavingsAccount.class));

        if (("productId").equals(session.getAttribute("expiredData"))) {
            session.removeAttribute("expiredData");
            model.addAttribute("expiredData", "productId");
        }
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

    @GetMapping("/details")
    public String getDetailedProduct(Model model, HttpSession session) {

        Long productId = (Long) session.getAttribute("productId");

        if (productId == null) {
            session.setAttribute("expiredData", "productId");
            return "redirect:/product/all";
        }
        return findProductAndRedirect(productId, model);
    }


    @PostMapping("/details")
    public String postDetailedProduct(@RequestParam(name = "id") Long id, Model model, HttpSession session) {

        if (id == null) {
            id = (Long) session.getAttribute("productId");
        } else {
            session.setAttribute("productId", id);
        }
        return findProductAndRedirect(id, model);
    }

    private String findProductAndRedirect(Long productId, Model model) {
        FinanceProduct product = productService.findById(productId);

        actionService.sortActionsByDate(product.getActions());
        model.addAttribute("product", product);

        if (product instanceof Investment) {
            return "productDetailsInvestment";
        } else if (product instanceof SavingsAccount) {

            productService.getAdditionalSavingsAccountViewData((SavingsAccount) product, model);
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
