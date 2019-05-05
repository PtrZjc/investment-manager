package pl.zajacp.investmentmanager.investments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;
    private final InvestmentRepository investmentRepository;

    @Autowired
    public ProductController(ProductRepository productRepository, InvestmentRepository investmentRepository) {
        this.productRepository = productRepository;
        this.investmentRepository = investmentRepository;
    }

    @GetMapping("/")
    public String homePage() {
        return "redirect:add";
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
    public String postInvestment(@ModelAttribute @Valid Investment investment, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("product",investment);
            return "productFormInvestment";
        }

        investmentRepository.save(investment);

        return "success";
    }


    @GetMapping("/add/savings-account")
    public String addSavingsAccount(SavingsAccount product, Model model) {
        model.addAttribute("product", new SavingsAccount());
        return "productFormSavingsAccount";
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
