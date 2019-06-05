package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    private final FinanceCalcService financeCalcService;

    @Autowired
    public ActionController(ProductService productService, ActionService actionService, UserService userService, FinanceCalcService financeCalcService) {
        this.productService = productService;
        this.actionService = actionService;
        this.userService = userService;
        this.financeCalcService = financeCalcService;
    }

    @GetMapping("/add")
    public String chooseProduct() {
        return "actionForm";
    }

        SavingsAccount product = (SavingsAccount) productService.findById(productId);

        if (!actionService.areSufficientFunds(actionDto, product)) {
            result.rejectValue("amount", "error.action.message.notSufficientFunds");
        }

        if (result.hasErrors()) {
            return "actionForm";
        }

        actionService.genBalanceChangeActions(actionDto, product);
        financeCalcService.recalculateCapitalizations(product, true);
        productService.sortActionsByDate(product);
        productService.getAdditionalSavingsAccountViewData(product, model);

        model.addAttribute("product", product);

        return "productDetailsSavingsAccount";
    }

    @PostMapping("/delete")
    public String deleteAction(@RequestParam(name = "id") Long id, HttpSession session) {
        Action action = actionService.findById(id);
        SavingsAccount product = (SavingsAccount) action.getProduct();
        actionService.delete(action);
        financeCalcService.recalculateCapitalizations(product, true);
        return "redirect:/product/details";
    }


}