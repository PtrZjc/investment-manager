package pl.zajacp.investmentmanager.data;

import org.springframework.stereotype.Service;
import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.Investment;
import pl.zajacp.investmentmanager.products.SavingsAccount;
import pl.zajacp.investmentmanager.user.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    public BigDecimal getTotalUserValueInMonth(User user, int minusMonths) {
        if (minusMonths < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal value = BigDecimal.ZERO;

        List<Investment> investments = user.getProducts().stream()
                .filter(p -> p instanceof Investment)
                .map(p -> (Investment) p)
                .collect(Collectors.toList());

        LocalDate investmentDate = minusMonths == 0 ?
                LocalDate.now() :
                YearMonth.now().atEndOfMonth().minusMonths(minusMonths);

        for (Investment investment : investments) {
            value = value.add(
                    investment.getActions().stream()
                            .filter(a -> a.getActionDate().isBefore(investmentDate))
                            .map(Action::getAfterActionValue)
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO));
        }
        //past capitalizations
        Predicate<Action> pastMonthCapitalization = a ->
                a.getActionDate().isEqual(YearMonth.now().minusMonths(minusMonths + 1L).atEndOfMonth()) &&
                        a.getActionType() == ActionType.CAPITALIZATION;
        value = value.add(
                user.getProducts().stream()
                        .filter(p -> p instanceof SavingsAccount)
                        .filter(p -> p.getOpenDate().isBefore(YearMonth.now().atDay(1)))
                        .map(FinanceProduct::getActions)
                        .flatMap(Collection::stream)
                        .filter(pastMonthCapitalization)
                        .map(Action::getAfterActionValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        //initial values of accounts opened at this month
        value = value.add(
                user.getProducts().stream()
                        .filter(p -> p instanceof SavingsAccount)
                        .filter(p -> p.getOpenDate().isAfter(YearMonth.now().minusMonths(1).atEndOfMonth()))
                        .map(FinanceProduct::getActions)
                        .flatMap(Collection::stream)
                        .filter(a -> a.getActionType() == ActionType.PRODUCT_OPEN)
                        .map(Action::getAfterActionValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        if (minusMonths == 0) {
            Predicate<Action> thisMonthBalanceChanges = a ->
                    a.getActionDate().isBefore(LocalDate.now()) &&
                            a.getActionDate().isAfter(YearMonth.now().minusMonths(1).atEndOfMonth()) &&
                            a.getActionType() == ActionType.BALANCE_CHANGE;

            value = value.add(
                    user.getProducts().stream()
                            .filter(p -> p instanceof SavingsAccount)
                            .map(FinanceProduct::getActions)
                            .flatMap(Collection::stream)
                            .filter(thisMonthBalanceChanges)
                            .map(Action::getBalanceChange)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return value.setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal getUserGainInMonth(List<Map<LocalDate, BigDecimal>> gains, int minusMonthsFromNow) {
        if (minusMonthsFromNow < 1) {
            throw new IllegalArgumentException();
        }

        Predicate<Map.Entry<LocalDate, BigDecimal>> withinMonth = e ->
                e.getKey().isBefore(YearMonth.now().minusMonths(minusMonthsFromNow + 1L).atDay(1)) &&
                        e.getKey().isAfter(YearMonth.now().minusMonths(minusMonthsFromNow - 1L).atEndOfMonth());

        return gains.stream()
                .flatMap(g -> g.entrySet().stream())
                .filter(withinMonth)
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal getActualSavingsAccountValue(SavingsAccount product) {
        BigDecimal value = BigDecimal.ZERO;

        //possible past capitalization
        if (product.getOpenDate().isBefore(YearMonth.now().atDay(1))) {
            Predicate<Action> pastMonthCapitalization = a ->
                    a.getActionDate().isEqual(YearMonth.now().minusMonths(1).atEndOfMonth()) &&
                            a.getActionType() == ActionType.CAPITALIZATION;
            value = value.add(
                    product.getActions().stream()
                            .filter(pastMonthCapitalization)
                            .map(Action::getAfterActionValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        //value if account was opened this month
        if (product.getOpenDate().isAfter(YearMonth.now().minusMonths(1).atEndOfMonth())) {
            value = value.add(
                    product.getActions().stream()
                            .filter(a -> a.getActionType() == ActionType.PRODUCT_OPEN)
                            .map(Action::getAfterActionValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        //balance changes
        Predicate<Action> thisMonthBalanceChanges = a ->
                a.getActionDate().isBefore(LocalDate.now()) &&
                        a.getActionDate().isAfter(YearMonth.now().minusMonths(1).atEndOfMonth()) &&
                        a.getActionType() == ActionType.BALANCE_CHANGE;

        value = value.add(
                product.getActions().stream()
                        .filter(thisMonthBalanceChanges)
                        .map(Action::getBalanceChange)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        return value;
    }
}
