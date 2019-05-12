package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.products.FinanceProduct;
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
    private static BigDecimal BELKA_TAX = new BigDecimal(0.81);

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
        close.setAfterActionValue(getInvestmentValueWithReturn(product));
        close.setActionDate(product.getOpenDate().plusMonths(product.getMonthsValid()));
        close.setProduct(product);

        actionRepository.save(open);
        actionRepository.save(close);
    }

    public void initializeSavingsAccountActions(SavingsAccount product) {
        /*
        * Initializes capitalization actions for the saving accounts. Generated are up to 12 capitalization actions,
        * one at last day of month, starting at product creation month up to validity date or 12 (in case validity
        * date is absent).
        * */

        BigDecimal lastValue = product.getValue();
        List<LocalDate> capitalizationDates = getCapitalizationDates(product);
        LocalDate openDate = product.getOpenDate();
        LocalDate endDate = product.getValidityDate();

        /*
        * The value of each subsequent generated capitalization is dependent on the previous one value.
        * First and last months need to be calculated proportionally.
        * */
        for (int i = 0; i < capitalizationDates.size(); i++) {
            Action capitalization = new Action();
            capitalization.setProduct(product);
            capitalization.setActionType(ActionType.CAPITALIZATION);
            capitalization.setActionDate(capitalizationDates.get(i));
            capitalization.setIsDone(false);

            if (i == 0) {
                if (openDate.getMonth() == product.getCreated().getMonth()){
                    capitalization.setAfterActionValue(getPartialMonthlyCapitalizedValue(lastValue, product, openDate,true));
                }else{
                    capitalization.setAfterActionValue(getMonthlyCapitalizedValue(lastValue,product,capitalizationDates.get(i)));
                }
            } else if (i == capitalizationDates.size() - 1) {
                capitalization.setAfterActionValue(getMonthlyCapitalizedValue(lastValue,product,capitalizationDates.get(i)));
            } else {
                if(endDate != null){
                    capitalization.setAfterActionValue(getPartialMonthlyCapitalizedValue(lastValue, product, endDate,false));
                }else{
                    capitalization.setAfterActionValue(getMonthlyCapitalizedValue(lastValue,product,capitalizationDates.get(i)));
                }
            }
            lastValue=capitalization.getAfterActionValue();
            actionRepository.save(capitalization);
        }
    }

    // actionDate
// afterActionValue
// isDone
    public BigDecimal getInvestmentValueWithReturn(Investment product) {
        long daysValid = DAYS.between(product.getOpenDate(),
                product.getOpenDate().plusMonths(product.getMonthsValid()));
        return product.getValue().add(product.getValue().multiply(product.getInterest())
                .multiply(new BigDecimal((double) daysValid / Year.of(product.getOpenDate().getYear()).length())) //factor of year
                .multiply(BELKA_TAX)
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

    public BigDecimal getMonthlyCapitalizedValue(BigDecimal value, FinanceProduct product, LocalDate date) {
        int daysInMonth = YearMonth.from(date).lengthOfMonth();
        int daysInYear = YearMonth.from(date).lengthOfYear();

        return value.add(value.multiply(product.getInterest())
                .multiply(new BigDecimal(1.0 * daysInMonth / daysInYear))
                .multiply(BELKA_TAX)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPartialMonthlyCapitalizedValue(BigDecimal value, FinanceProduct product,
                                                        LocalDate date, boolean firstMonthHalf) {
        /*
        * Date used in first and last have to be open/close date, as days number is used for calculation.
        * All other months in between are using capitalization date.
        * */

        int daysInMonth = YearMonth.from(date).lengthOfMonth();
        int daysInYear = YearMonth.from(date).lengthOfYear();
        BigDecimal monthFraction;

        if (firstMonthHalf) {
            monthFraction = new BigDecimal(1.0 * date.getDayOfMonth() / daysInMonth);
        } else {
            monthFraction = new BigDecimal(1.0 - (1.0 * date.getDayOfMonth() / daysInMonth));
        }

        return value.add(value.multiply(product.getInterest())
                .multiply(new BigDecimal(1.0 * daysInMonth / daysInYear))
                .multiply(monthFraction)
                .multiply(BELKA_TAX)).setScale(2, RoundingMode.HALF_UP);
    }
}
