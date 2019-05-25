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
import java.util.function.Predicate;

import static java.time.temporal.ChronoUnit.DAYS;
//TODO Change planned value of action
//TODO Widthdraw can be only set in current month

@Service
@Transactional
public class ActionService {

    private final ActionRepository actionRepository;

    public ActionService(ActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    public Action createSameMonthLater(Action baseAction) {
        Action action = new Action();
        action.setBalanceChange(baseAction.getBalanceChange());
        action.setProduct(baseAction.getProduct());
        action.setActionDate(baseAction.getActionDate().plusMonths(1));
        action.setActionType(baseAction.getActionType());
        action.setNotes(baseAction.getNotes());
        return action;
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
            Action capitalizationAction = new Action();
            capitalizationAction.setProduct(product);
            capitalizationAction.setActionType(ActionType.CAPITALIZATION);
            capitalizationAction.setActionDate(capitalizationDates.get(i));
            capitalizationAction.setIsDone(false);

            BigDecimal capitalizationChange;

            if (i == 0) {
                //first month
                capitalizationChange = openDate.getMonth() == product.getCreated().getMonth() ?
                        getPartialCapitalizedValue(lastValue, product, openDate, true) :
                        getCapitalization(lastValue, product, capitalizationDates.get(i));
            } else if (i < capitalizationDates.size() - 1) {
                //all months but last
                capitalizationChange = getCapitalization(lastValue, product, capitalizationDates.get(i));
            } else {
                //last month
                capitalizationChange = endDate != null ?
                        getPartialCapitalizedValue(lastValue, product, endDate, false) :
                        getCapitalization(lastValue, product, capitalizationDates.get(i));
            }

            capitalizationAction.setAfterActionValue(lastValue.add(capitalizationChange).setScale(2, RoundingMode.HALF_UP));

            lastValue = capitalizationAction.getAfterActionValue();

            actionRepository.save(capitalizationAction);
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

    public BigDecimal getCapitalization(BigDecimal value, SavingsAccount product, LocalDate date) {
        return getCapitalizedProfit(value, product, date).multiply(new BigDecimal(0.81));
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

        return getCapitalizedProfit(value, product, date).multiply(monthFraction).multiply(new BigDecimal(0.81));
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
        } else if (fullProfitFraction.compareTo(BigDecimal.ZERO) < 0) {
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

    public void generateBalanceChangeActions() {
        //tutaj będzie multiple selection pokazujący nazwy (numery) miesięcy
    }


    public void recalculateCapitalizations(SavingsAccount product, boolean fromTodayCalculation) {
        /*
         * Recalculates all capitalization actions taking into account all withdraws and payments. Recalculates either
         * all actions for given product (with past ones), or all future actions starting from current month.
         * It divides actions into months and changes capitalization values in place, month after month.
         */

        product.setActions(actionRepository.findByProductOrderByActionDateAscAfterActionValueAsc(product));
        List<Action> actions = product.getActions();

        LocalDate currentDate = fromTodayCalculation ? LocalDate.now() : actions.get(0).getActionDate();
        BigDecimal currentValue = product.getValue();

        //test:
        //currentDate = LocalDate.parse("2019-06-25", DateTimeFormatter.ISO_LOCAL_DATE);

        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        int upperBoundIndex, lowerBoundIndex = 0;

        for (int i = 0; i < actions.size(); i++) {
            int actionYear = actions.get(i).getActionDate().getYear();
            int actionMonth = actions.get(i).getActionDate().getMonthValue();

            //In case of calculation from actual date, here it omits past months
            if (fromTodayCalculation && currentMonth > actionMonth && currentYear >= actionYear) {
                lowerBoundIndex = i + 1;
                continue;
            }

            if (actions.get(i).getActionType() == ActionType.CAPITALIZATION) {
                upperBoundIndex = i;
                fixMonthlyCapitalization(product, lowerBoundIndex, upperBoundIndex, currentValue);
                System.out.println(lowerBoundIndex + " / " + upperBoundIndex);
                currentValue = actions.get(i).getAfterActionValue();
                lowerBoundIndex = i + 1;
            }
        }

        actionRepository.saveAll(actions);
    }

    public void fixMonthlyCapitalization(SavingsAccount product, int lowerMonthBound, int upperMonthBound, BigDecimal initialValue) {
        List<Action> actions = product.getActions();
        Action capitalization = actions.get(upperMonthBound);
        BigDecimal withdraws = BigDecimal.ZERO;
        BigDecimal totalValueChange = initialValue;
        BigDecimal totalCapitalization = BigDecimal.ZERO;

        for (int i = lowerMonthBound; i < upperMonthBound; i++) {
            BigDecimal currentChange = actions.get(i).getBalanceChange();
            if (currentChange.compareTo(BigDecimal.ZERO) > 0) {
                //payments capitalizations
                totalCapitalization = totalCapitalization.add(getPartialCapitalizedValue
                        (currentChange, product, actions.get(i).getActionDate(), false));
            } else {
                //widthdraws capitalizations
                totalCapitalization = totalCapitalization.subtract(getPartialCapitalizedValue
                        (currentChange.abs(), product, actions.get(i).getActionDate(), false));
                withdraws = withdraws.subtract(currentChange);
            }
            totalValueChange = totalValueChange.add(currentChange);
            actions.get(i).setAfterActionValue(totalValueChange);
        }

        //rest of unchanged value capitalization
        totalCapitalization = totalCapitalization.add(getCapitalization(initialValue.subtract(withdraws), product, capitalization.getActionDate()));

        capitalization.setAfterActionValue(totalValueChange.add(totalCapitalization).setScale(2, RoundingMode.HALF_UP));

    }

    public void save(Action action) {
        actionRepository.save(action);
    }

    public void genBalanceChangeActions(ActionDto actionDto, SavingsAccount product) {
        Action action = new Action();
        action.setActionType(ActionType.BALANCE_CHANGE);
        action.setActionDate(actionDto.getActionDate());
        action.setProduct(product);

        if (!actionDto.getIsNegative()) {
            action.setBalanceChange(actionDto.getAmount());
        } else {
            action.setBalanceChange(actionDto.getAmount().negate());
        }

        List<Action> actions = new ArrayList<>(Collections.singletonList(action));

        if (!actionDto.getIsSingle()) {
            LocalDate latestDate = product.getActions().stream()
                    .max(Comparator.comparing(Action::getActionDate))
                    .map(Action::getActionDate)
                    .orElseThrow(NullPointerException::new);

            while (action.getActionDate().isBefore(latestDate.minusMonths(1))) {
                action = createSameMonthLater(action);
                actions.add(action);
            }
        }

        actionRepository.saveAll(actions);
        product.getActions().addAll(actions);
    }

    public void delete(Action action) {
        actionRepository.delete(action);
    }

    public boolean areSufficientFunds(ActionDto actionDto, SavingsAccount product) {
        if (!actionDto.getIsNegative()) {
            return true;
        }

        Predicate<Action> previousMonthCap =
                x -> x.getActionType() == ActionType.CAPITALIZATION &&
                        x.getActionDate().getMonth().minus(1) == actionDto.getActionDate().getMonth();

        BigDecimal capValue = product.getActions().stream()
                .filter(previousMonthCap)
                .min(Comparator.comparing(Action::getActionDate))
                .map(Action::getAfterActionValue)
                .orElseThrow(NullPointerException::new);

        return actionDto.getAmount().compareTo(capValue) < 0;
    }
}