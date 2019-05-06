package pl.zajacp.investmentmanager.investments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String homePage() {
        return "redirect:/product/add";
    }

    @GetMapping("/add")
    public String chooseProduct() {
        return "productChoose";
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

        product.setInterest(product.getInterest().divide(new BigDecimal(100)));
        productRepository.save(product);

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

        product.setInterest(product.getInterest().divide(new BigDecimal(100)));
        productRepository.save(product);

        return "success";
    }

//    @PostMapping("/")
//    public String postProduct(@ModelAttribute @Valid Product product, BindingResult result) {
//        if (result.hasErrors()) {
//            return "formProduct";
//        }
//
//        if (product.getId() == null) {
//            productDao.save(product);
//        } else {
//            productDao.update(product);
//        }
//        return "redirect:all";
//    }

}
