package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Transactional
public class ActionService {

    private final ActionRepository actionRepository;

    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    public void initializeInvestmentActions(Investment product) {

        Action open = new Action();
        open.setActionType(ActionType.PRODUCT_OPEN);
        open.setAfterActionValue(product.getValue());
        open.setActionDate(product.getOpenDate());
        open.setProduct(product);

        Action close = new Action();
        close.setActionType(ActionType.PRODUCT_CLOSE);
        close.setAfterActionValue(investmentValueWithReturn(product));
        close.setActionDate(product.getOpenDate().plusMonths(product.getMonthsValid()));
        close.setProduct(product);

        actionRepository.save(open);
        actionRepository.save(close);
    }

    public void initializeSavingsAccountActions(SavingsAccount product) {
//        int openMonths = (product.getOpenDate().getYear() - LocalDate.now().getYear()) * 12 + product.getOpenDate().getMonthValue();
//        int closeMonths = (product.getValidityDate().getYear() - LocalDate.now().getYear()) * 12 + product.getValidityDate().getMonthValue();
//        int monthsValid = closeMonths - openMonths;
    }

    public BigDecimal investmentValueWithReturn(Investment product) {
        long daysValid = DAYS.between(product.getOpenDate(),
                product.getOpenDate().plusMonths(product.getMonthsValid()));
        return product.getValue().add(product.getValue().multiply(product.getInterest())
                .multiply(new BigDecimal((double) daysValid / Year.of(product.getOpenDate().getYear()).length())) //factor of year
                .multiply(new BigDecimal(0.81)) //Belka tax
                .setScale(2, RoundingMode.HALF_UP));
    }

    public List<LocalDate> getCapitalizationDates(SavingsAccount product) {
        /*
         * Create list of dates consisting of last days of months. Last date is one after validity date
         * or after 12 months if the validityDate is absent.
         */

        List<LocalDate> capitalizationDates = new ArrayList<>();
        LocalDate currentDate = product.getCreated();
        LocalDate validityDate = product.getValidityDate();
        if (validityDate == null) {
            validityDate = currentDate.plusMonths(12);
        }
        int index = 1;

        do {
            YearMonth month = YearMonth.from(currentDate);
            LocalDate capitalizationDay = month.atEndOfMonth();
            capitalizationDates.add(capitalizationDay);
            currentDate = capitalizationDay.plusDays(index++);
        } while (currentDate.isBefore(validityDate));

        return capitalizationDates;
    }
}
