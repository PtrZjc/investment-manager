package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.zajacp.investmentmanager.products.ProductService;
import pl.zajacp.investmentmanager.products.SavingsAccount;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/product/operation")
public class ActionController {

    private final ProductService productService;
    private final ActionService actionService;

    public ActionController(ProductService productService, ActionService actionService) {
        this.productService = productService;
        this.actionService = actionService;
    }

    @GetMapping("/add")
    public String addAction(Model model) {

        model.addAttribute("actionDto", new ActionDto());
        return "actionForm";
    }

    @PostMapping("/add")
    public String saveAction(@ModelAttribute("actionDto") @Valid ActionDto actionDto,
                             BindingResult result, HttpSession session, Model model) {
        Long productId = (Long) session.getAttribute("productId");

        if (productId == null) {
            session.setAttribute("expiredData", "productId");
            return "redirect:/product/all";
        }

        if (result.hasErrors()) {
            return "actionForm";
        }

        SavingsAccount product = (SavingsAccount) productService.findById(productId);

        if (!actionService.areSufficientFunds(actionDto, product)) {
            result.rejectValue("amount", "error.notSufficientFunds");
        }

        if (result.hasErrors()) {
            return "actionForm";
        }

        actionService.genBalanceChangeActions(actionDto, product);
        actionService.recalculateCapitalizations(product);
        productService.getAdditionalSavingsAccountViewData(product, model);

        model.addAttribute("product", product);

        return "productDetailsSavingsAccount";
    }

    @PostMapping("/delete")
    public String deleteAction(@RequestParam(name = "id") Long id, HttpSession session) {
        Action action = actionService.findById(id);
        SavingsAccount product = (SavingsAccount) action.getProduct();
        actionService.delete(action);
        actionService.recalculateCapitalizations(product);
        return "redirect:/product/details";
    }

}