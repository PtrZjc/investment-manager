package pl.zajacp.investmentmanager.data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.products.Investment;
import pl.zajacp.investmentmanager.products.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Transactional
public class FinanceCalcService {

    private final BigDecimal BELKA_TAX = BigDecimal.valueOf(0.81);

    public FinanceCalcService() {
    }

    public BigDecimal getInvestmentValueWithReturn(Investment product) {
        long daysValid = DAYS.between(product.getOpenDate(),
                product.getOpenDate().plusMonths(product.getMonthsValid()));

        return product.getValue().add(product.getValue().multiply(product.getInterest())
                .multiply(new BigDecimal(1.0 * daysValid / Year.of(product.getOpenDate().getYear()).length()))
                .multiply(BELKA_TAX).setScale(2, RoundingMode.DOWN));
    }

    public List<LocalDate> getCapitalizationDates(SavingsAccount product) {
        /*
         * Create list of dates consisting of last days of months for a whole year.
         */
        List<LocalDate> capitalizationDates = new ArrayList<>();
        LocalDate currentDate = product.getCreated();

        for (int i = 0; i < 12; i++) {
            YearMonth yearMonth = YearMonth.from(currentDate.plusMonths(i));
            capitalizationDates.add(yearMonth.atEndOfMonth());
        }
        return capitalizationDates;
    }

    public BigDecimal getMonthCapitalization(BigDecimal value, SavingsAccount product, LocalDate date) {
        return getCapitalizedProfit(value, product, date, product.getInterest());
    }

    public BigDecimal getAfterPromotionCapitalization(BigDecimal value, SavingsAccount product, LocalDate date) {
        return getCapitalizedProfit(value, product, date, product.getInterestAboveLimit());
    }

    public BigDecimal getPartialMonthCapitalization(BigDecimal value, SavingsAccount product, LocalDate date, BigDecimal interestRate) {
        int daysInFirstMonth = YearMonth.from(date).lengthOfMonth();
        BigDecimal applicableMonthFraction = new BigDecimal(1.0 - (1.0 * date.getDayOfMonth() / daysInFirstMonth));
        return getCapitalizedProfit(value, product, date, interestRate).multiply(applicableMonthFraction);
    }

    public BigDecimal getFirstMonthCapitalization(BigDecimal value, SavingsAccount product) {
        /*
         * Date parameter needs to be account open date, as days number is used for calculation.
         * All other months in between are using capitalization dates.
         */
        LocalDate openDate = product.getOpenDate();
        return getPartialMonthCapitalization(value, product, openDate, product.getInterest());
    }

    public BigDecimal getSelectedCapitalization(BigDecimal value, SavingsAccount product, LocalDate date, MonthType monthType) {
        BigDecimal capitalization = null;
        switch (monthType) {
            case FIRST:
                capitalization = getFirstMonthCapitalization(value, product);
                break;
            case VALID:
                capitalization = getMonthCapitalization(value, product, date);
                break;
            case AFTER_PROMOTION:
                capitalization = getAfterPromotionCapitalization(value, product, date);
                break;
        }
        return capitalization;
    }

    private BigDecimal getCapitalizedProfit(BigDecimal value, SavingsAccount product, LocalDate date, BigDecimal interestRate) {
        /*
         * date here is used only to calculate year fraction, therefore it does not matter from which action
         * within same month it comes.
         * */

        BigDecimal yearFraction = new BigDecimal
                (1.0 * YearMonth.from(date).lengthOfMonth() / YearMonth.from(date).lengthOfYear());

        //equation: value*(1+(interest*yearFraction))
        BigDecimal valueWithoutLimit = value.multiply(BigDecimal.ONE.add(interestRate.multiply(yearFraction)));

        /*following calculation is necesarry in case the value exceeded promotional account limit */

        //equation: 1-(withoutLimitValue-valueLimit)/(withoutLimitValue-value)
        BigDecimal fullProfitFraction = BigDecimal.ONE.subtract((valueWithoutLimit.subtract(product.getValueLimit())
                .divide(valueWithoutLimit.subtract(value), BigDecimal.ROUND_HALF_UP)));
        if (fullProfitFraction.compareTo(BigDecimal.ONE) > 0) {
            fullProfitFraction = BigDecimal.ONE;
        } else if (fullProfitFraction.compareTo(BigDecimal.ZERO) < 0) {
            fullProfitFraction = BigDecimal.ZERO;
        }

        //value with profit generated by main interest rate
        //equation: value*(1+interest/yearFraction)*fullProfitFraction
        BigDecimal fullProfitValue = value.multiply(BigDecimal.ONE.add(interestRate.multiply(yearFraction)))
                .multiply(fullProfitFraction);

        //value with profit generated by interest rate above limit
        //equation (1-fullProfitFraction)*value*(1+interestAbove/yearFraction)
        BigDecimal limitedProfitValue = BigDecimal.ONE.subtract(fullProfitFraction)
                .multiply(value).multiply(BigDecimal.ONE.add(product.getInterestAboveLimit()));

        return fullProfitValue.add(limitedProfitValue).subtract(value).multiply(BELKA_TAX);
    }

    public List<Action> recalculateCapitalizations(SavingsAccount product, boolean fromTodayCalculation) {
        /*
         * Recalculates all capitalization actions including all withdraws and payments. Recalculates either
         * all actions for given product (with past ones), or all future actions starting from current month.
         * It divides actions into months and changes capitalization values in place, month after month.
         */
        List<Action> actions = product.getActions();
        LocalDate currentDate = fromTodayCalculation ? LocalDate.now() : actions.get(0).getActionDate();
        BigDecimal currentValue = product.getValue();
        YearMonth currentYearMonth = YearMonth.from(currentDate);

        int monthCapitalizationIndex, firstMonthActionIndex = 0;
        int endDateIndex = getEndDateIndex(actions);

        for (int i = 0; i < actions.size(); i++) {
            YearMonth actionYearMonth = YearMonth.from(actions.get(i).getActionDate());

            //In case of calculation from actual date, here it omits past months
            if (fromTodayCalculation && currentYearMonth.isAfter(actionYearMonth)) {
                firstMonthActionIndex = i + 1;
                continue;
            }

            if (actions.get(i).getActionType() == ActionType.CAPITALIZATION) {
                MonthType monthType;
                monthCapitalizationIndex = i;

                if (firstMonthActionIndex == 0) {
                    monthType = MonthType.FIRST;
                } else if (monthCapitalizationIndex < endDateIndex) {
                    monthType = MonthType.VALID;
                } else {
                    monthType = MonthType.AFTER_PROMOTION;
                }

                List<Action> currentMonthActions = actions.subList(firstMonthActionIndex, monthCapitalizationIndex + 1);

                fixMonthlyCapitalization(currentValue, currentMonthActions, monthType);

                System.out.println(firstMonthActionIndex + " / " + monthCapitalizationIndex);
                currentValue = actions.get(i).getAfterActionValue();
                firstMonthActionIndex = monthCapitalizationIndex + 1;
            }
        }
        return actions;
    }

    private void fixMonthlyCapitalization(BigDecimal currentValue, List<Action> currentMonthActions, MonthType monthType) {

        SavingsAccount product = (SavingsAccount) currentMonthActions.get(0).getProduct();
        BigDecimal withdraws = BigDecimal.ZERO;
        BigDecimal totalValueChange = currentValue;
        BigDecimal totalCapitalization = BigDecimal.ZERO;
        Action capitalizationAction = currentMonthActions.get(currentMonthActions.size() - 1);

        for (Action action : currentMonthActions) {
            BigDecimal currentChange = action.getBalanceChange();
            if (currentChange == null) {
                continue;
            }
            LocalDate actionDate = action.getActionDate();

            if (currentChange.compareTo(BigDecimal.ZERO) > 0) {
                totalCapitalization = totalCapitalization.add(getSelectedCapitalization(
                        currentChange, product, actionDate, monthType));
            } else {
                totalCapitalization = totalCapitalization.add(getSingleWidthdrawCapitalization(
                        currentChange.abs(), product, actionDate, monthType));
                withdraws = withdraws.subtract(currentChange);
            }

            totalValueChange = totalValueChange.add(currentChange);
            action.setAfterActionValue(totalValueChange.setScale(2, RoundingMode.DOWN));
        }

        BigDecimal baseValueCapitalization = getSelectedCapitalization
                (currentValue.subtract(withdraws), product, capitalizationAction.getActionDate(), monthType);

        totalCapitalization = totalCapitalization.add(baseValueCapitalization);
        capitalizationAction.setAfterActionValue(totalValueChange.add(totalCapitalization).setScale(2, RoundingMode.DOWN));
    }

    private BigDecimal getSingleWidthdrawCapitalization(BigDecimal value, SavingsAccount product,
                                                        LocalDate date, MonthType monthType) {

        BigDecimal interest = monthType != MonthType.AFTER_PROMOTION ? product.getInterest() : product.getInterestAboveLimit();
        BigDecimal wholeMonth = getCapitalizedProfit(value, product, date, interest);
        BigDecimal secondHalf = getPartialMonthCapitalization(value, product, date, interest);
        BigDecimal firstMonthInitialDays = BigDecimal.ZERO;

        if (monthType == MonthType.FIRST) {
            firstMonthInitialDays = wholeMonth.subtract(
                    getPartialMonthCapitalization(value, product, product.getOpenDate(), interest));
        }
        return wholeMonth.subtract(secondHalf).subtract(firstMonthInitialDays);
    }

    private int getEndDateIndex(List<Action> actions) {
        int endDateIndex = 0;
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i).getActionType().equals(ActionType.PRODUCT_CLOSE)) {
                endDateIndex = i;
                break;
            }
        }
        if (endDateIndex == 0) {
            throw new IllegalArgumentException();
        }
        return endDateIndex;
    }

    public Map<LocalDate, BigDecimal> getGain(List<Action> actions) {
        /*
         * Returns map of dates when capitalization occured with calculated gain from open date.
         * */
        Map<LocalDate, BigDecimal> gains = new LinkedHashMap<>();
        BigDecimal currentVal = actions.get(0).getAfterActionValue();

        for (int i = 1; i < actions.size(); i++) {
            Action currentAction = actions.get(i);
            if (currentAction.getActionType() == ActionType.BALANCE_CHANGE) {
                currentVal = currentVal.add(actions.get(i).getBalanceChange());
            } else if (currentAction.getActionType() == ActionType.CAPITALIZATION || currentAction.getProduct() instanceof Investment) {
                gains.put(currentAction.getActionDate(), currentAction.getAfterActionValue().subtract(currentVal)
                        .setScale(2, RoundingMode.DOWN));
            }
        }
        return gains;
    }



}