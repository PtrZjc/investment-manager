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
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Transactional
public class ActionService {

    //TODO Custom payments in and out

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
                if (openDate.getMonth() == product.getCreated().getMonth()) {
                    capitalization.setAfterActionValue(getPartialCapitalizedValue(lastValue, product, openDate, true));
                } else {
                    capitalization.setAfterActionValue(getCapitalizedValue(lastValue, product, capitalizationDates.get(i)));
                }
            } else if (i < capitalizationDates.size() - 1) {
                capitalization.setAfterActionValue(getCapitalizedValue(lastValue, product, capitalizationDates.get(i)));
            } else {
                if (endDate != null) {
                    capitalization.setAfterActionValue(getPartialCapitalizedValue(lastValue, product, endDate, false));
                } else {
                    capitalization.setAfterActionValue(getCapitalizedValue(lastValue, product, capitalizationDates.get(i)));
                }
            }
            lastValue = capitalization.getAfterActionValue();

            actionRepository.save(capitalization);
        }
    }


    public BigDecimal getInvestmentValueWithReturn(Investment product) {
        long daysValid = DAYS.between(product.getOpenDate(),
                product.getOpenDate().plusMonths(product.getMonthsValid()));

        return product.getValue().add(product.getValue().multiply(product.getInterest())
                .multiply(new BigDecimal(1.0 * daysValid / Year.of(product.getOpenDate().getYear()).length()))
                .multiply(new BigDecimal(0.81)).setScale(2, RoundingMode.HALF_UP));
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

    public BigDecimal getCapitalizedValue(BigDecimal value, SavingsAccount product, LocalDate date) {
        return value.add(getCapitalizedProfit(value, product, date).multiply(new BigDecimal(0.81)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPartialCapitalizedValue(BigDecimal value, SavingsAccount product,
                                                 LocalDate date, boolean firstMonthHalf) {
        /*
         * Date used in first and last have to be open/close date, as days number is used for calculation.
         * All other months in between are using capitalization date.
         */

        int daysInMonth = YearMonth.from(date).lengthOfMonth();
        int daysInYear = YearMonth.from(date).lengthOfYear();
        BigDecimal monthFraction;

        if (firstMonthHalf) {
            monthFraction = new BigDecimal(1.0 * date.getDayOfMonth() / daysInMonth);
        } else {
            monthFraction = new BigDecimal(1.0 - (1.0 * date.getDayOfMonth() / daysInMonth));
        }

        return value.add(getCapitalizedProfit(value, product, date).multiply(monthFraction)
                .multiply(new BigDecimal(0.81))).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getCapitalizedProfit(BigDecimal value, SavingsAccount product, LocalDate date) {

        BigDecimal yearFraction = new BigDecimal
                (1.0 * YearMonth.from(date).lengthOfMonth() / YearMonth.from(date).lengthOfYear());

        //equation: value*(1+(interest*yearFraction))
        BigDecimal valueWithoutLimit = value.multiply(BigDecimal.ONE.add(product.getInterest().multiply(yearFraction)));

        //equation: 1-(withoutLimitValue-valueLimit)/(withoutLimitValue-value)
        BigDecimal fullProfitFraction = BigDecimal.ONE.subtract((valueWithoutLimit.subtract(product.getValueLimit())
                .divide(valueWithoutLimit.subtract(value), BigDecimal.ROUND_HALF_UP)));
        if (fullProfitFraction.compareTo(BigDecimal.ONE) > 0) {
            fullProfitFraction = BigDecimal.ONE;
        }else if(fullProfitFraction.compareTo(BigDecimal.ZERO) < 0){
            fullProfitFraction = BigDecimal.ZERO;
        }

        //value with profit generated by main intere7st rate
        //equation: value*(1+interest/yearFraction)*fullProfitFraction
        BigDecimal fullProfitValue = value.multiply(BigDecimal.ONE.add(product.getInterest().multiply(yearFraction)))
                .multiply(fullProfitFraction);

        //value with profit generated by interest rate above limit
        //equation (1-fullProfitFraction)*value*(1+interestAbove/yearFraction)
        BigDecimal limitedProfitValue = BigDecimal.ONE.subtract(fullProfitFraction)
                .multiply(value).multiply(BigDecimal.ONE.add(product.getInterestAboveLimit()));

        return fullProfitValue.add(limitedProfitValue).subtract(value);
    }

    public void generateBalanceChangeActions(){
        //tutaj będzie multiple selection pokazujący nazwy (numery) miesięcy
    }



    public void applyBalanceChanges(SavingsAccount product, List<Action> balanceChanges){

        /*
         * 1. W miesiącu iteruj przez wszystkie akcje po kolei i zlicz sumę wypłat oraz wpłat niezależnie.
         * 2. Jeśli suma wypłat przekroczyła wartość w danym miesiącu, to wywal błąd, że za dużo.
         *    Jeśli nie przekroczyła, to policz odejmij sumę od wartości tego miesiąca i przekalkuluj z tego pełną kapitalizację
         *    (lub częściową, jeśli to pierwszy lub ostatni miesiąc.
         * 3. Każda akcja będzie osobno kalkulowana
         * */


//
//        //for each month:
//        //
//        Set<Integer> x = new TreeSet<>();
//
//
//        Comparator<Action> byDate
//                = (Action action1, Action action2) -> action1.getActionDate() player1.getRanking() - player2.getRanking();
//

    }






}
