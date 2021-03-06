package pl.zajacp.investmentmanager.actionmanagement;

import org.junit.Test;
import pl.zajacp.investmentmanager.data.FinanceCalcService;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.Investment;
import pl.zajacp.investmentmanager.products.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FinanceCalcServiceTest {

    private static FinanceCalcService financeCalcService = new FinanceCalcService();
    private static SavingsAccount savingsAccount;
    private static Investment investment;
    private static List<Action> actions;

    private static void prepareProducts() {
        LocalDate openDate = LocalDate.parse("2018-03-01", DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate validityDate = LocalDate.parse("2018-09-10", DateTimeFormatter.ISO_LOCAL_DATE);
        savingsAccount = mock(SavingsAccount.class);
        investment = mock(Investment.class);


        for (FinanceProduct prod : new FinanceProduct[]{savingsAccount, investment}) {
            when(prod.getOpenDate()).thenReturn(openDate);
            when(prod.getCreated()).thenReturn(openDate.plusMonths(1));
            when(prod.getInterest()).thenReturn(BigDecimal.valueOf(0.035));
            when(prod.getValue()).thenReturn(BigDecimal.valueOf(1000));
        }
        when(investment.getMonthsValid()).thenReturn(12L);
        when(savingsAccount.getValidityDate()).thenReturn(validityDate);
        when(savingsAccount.getInterestAboveLimit()).thenReturn(BigDecimal.valueOf(0.005));
        when(savingsAccount.getValueLimit()).thenReturn(BigDecimal.valueOf(10000));
    }

    public static void prepareActions() {
        String[] dates = {"2019-05-15", "2019-05-20", "2019-05-25", "2019-05-31", "2019-06-15", "2019-06-20",
                "2019-06-25", "2019-06-30", "2019-06-30", "2019-07-15", "2019-07-20", "2019-07-25", "2019-07-31"};

        Integer[] balanceChanges = {500, -300, 700, null, 300, -500, 1000, 300, null, 600, -500, -300, null};

        Double[] afterActionValues = {null, null, null, 5004.44, null, null, null, null, 5021.10, null, null, null, 5025.00};

        actions = new ArrayList<>(13);
        for (int i = 0; i < 13; i++) {
            actions.add(mock(Action.class));
            if (i == 3 || i == 8 || i == 12) {
                when(actions.get(i).getActionType()).thenReturn(ActionType.CAPITALIZATION);
                when(actions.get(i).getAfterActionValue()).thenReturn(new BigDecimal(afterActionValues[i]));
            } else {
                when(actions.get(i).getActionType()).thenReturn(ActionType.BALANCE_CHANGE);
                when(actions.get(i).getBalanceChange()).thenReturn(new BigDecimal(balanceChanges[i]));
            }
            when(actions.get(i).getActionDate()).thenReturn(LocalDate.parse(dates[i], DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }

    @Test
    public void shouldInvestmentValueWithReturn() {
        //given
        prepareProducts();
        BigDecimal expectedReturn = new BigDecimal(1000 + (1000 * 0.035) * 0.81).setScale(2,RoundingMode.HALF_DOWN);
        //when
        BigDecimal returnValue = financeCalcService.getInvestmentValueWithReturn(investment);
        //then
        assertThat(returnValue, is(expectedReturn));
    }

    @Test
    public void shouldGetCapitalizationDates() {
        //given
        prepareProducts();
        LocalDate lastCapitalisationDate = LocalDate.parse("2018-09-30", DateTimeFormatter.ISO_LOCAL_DATE);
        //when
        List<LocalDate> dates = financeCalcService.getCapitalizationDates(savingsAccount);
        //then
        assertThat(dates, hasItem(lastCapitalisationDate));
        assertThat(dates.size(), is(12));
    }

    @Test
    public void shouldCapitalizedValue() {
        //given
        prepareProducts();
        BigDecimal expectedValue = new BigDecimal(1000 + (1000 * 0.035 * (30.0 / 365) * 0.81))
                .setScale(2, RoundingMode.HALF_DOWN);
        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();
        //when
        BigDecimal CapitalizedValue = value.add(financeCalcService.getMonthCapitalization(value, savingsAccount, date))
                .setScale(2, RoundingMode.HALF_DOWN);
        //then

        assertThat(CapitalizedValue, is(expectedValue));
    }

    @Test
    public void shouldCapitalizedValueAtLimit() {
        //given
        prepareProducts();
        when(savingsAccount.getValue()).thenReturn(BigDecimal.valueOf(9980));

        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();

        BigDecimal excelCalculation = BigDecimal.valueOf(10008.69);
        BigDecimal excelFloatError = excelCalculation.multiply(BigDecimal.valueOf(0.00003));
        //when

        BigDecimal valueCapitalizedAtLimit = value.add(financeCalcService.getMonthCapitalization(value, savingsAccount, date));
        //then
        assertThat(valueCapitalizedAtLimit, is(closeTo(excelCalculation, excelFloatError)));
    }

    @Test
    public void shouldCapitalizedValueAtLimitLarge() {

        //given
        prepareProducts();
        when(savingsAccount.getValue()).thenReturn(BigDecimal.valueOf(499000));
        when(savingsAccount.getValueLimit()).thenReturn(BigDecimal.valueOf(500000));
        when(savingsAccount.getInterest()).thenReturn(BigDecimal.valueOf(0.05));
        when(savingsAccount.getInterestAboveLimit()).thenReturn(BigDecimal.valueOf(0.002));

        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();

        BigDecimal excelCalculation = BigDecimal.valueOf(500235.43);
        BigDecimal excelFloatError = excelCalculation.multiply(BigDecimal.valueOf(0.00003));
        //when
        BigDecimal valueCapitalizedAtLimit = value.add(financeCalcService.getMonthCapitalization(value, savingsAccount, date));
        //then
        assertThat(valueCapitalizedAtLimit, is(closeTo(excelCalculation, excelFloatError)));
    }

    @Test
    public void shouldPartialCapitalizedValue() {
        //given
        prepareProducts();
        BigDecimal fullMonthExpectedValue = new BigDecimal(1000 + (1000 * 0.035 * (30.0 / 365) * 0.81))
                .setScale(2, RoundingMode.DOWN);
        BigDecimal secondHalfExpectedValue = new BigDecimal(1000 + (1000 * 0.035 * (30.0 / 365) * 0.81 * (1 - 10.0 / 30)))
                .setScale(2, RoundingMode.DOWN);
        BigDecimal firstHalfExpectedValue = fullMonthExpectedValue.subtract(secondHalfExpectedValue);

        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();

//        LocalDate date = savingsAccount.getOpenDate();
        //when
        BigDecimal fullMonth = value.add(financeCalcService.getMonthCapitalization
                (value, savingsAccount, date)).setScale(2, RoundingMode.DOWN);
        BigDecimal secondHalf = value.add(financeCalcService.getPartialMonthCapitalization(
                value, savingsAccount, date, savingsAccount.getInterest())).setScale(2, RoundingMode.DOWN);
        BigDecimal firstHalf = fullMonth.subtract(secondHalf);
                //then
        assertThat(firstHalf, is(firstHalfExpectedValue));
        assertThat(secondHalf, is(secondHalfExpectedValue));
    }

    @Test
    public void shouldGetGain() {
        //given
        actions = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            actions.add(mock(Action.class));
        }

        for (int i = 0; i < 4; i++) {
            when(actions.get(i*2+1).getBalanceChange()).thenReturn(BigDecimal.valueOf(500));
            when(actions.get(i*2+1).getActionType()).thenReturn(ActionType.BALANCE_CHANGE);
        }

        List<LocalDate> capitDates = new ArrayList<>();
        for (String textDate : new String[]{"2019-05-01","2019-05-31", "2019-06-30", "2019-07-31", "2019-08-31"}) {
            capitDates.add(LocalDate.parse(textDate, DateTimeFormatter.ISO_LOCAL_DATE));
        }

        List<BigDecimal> capitValues =  new ArrayList<>();
        for (Double capDoubleVal : new Double[]{10000.00,10534.62,11069.85,11608.15,12148.30}) {
            capitValues.add(new BigDecimal(capDoubleVal));
        }

        Map<Integer, Object[]> initValues = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            initValues.put(i*2,new Object[]{capitValues.get(i),capitDates.get(i),ActionType.CAPITALIZATION});
        }

        initValues.forEach((i, v) -> when(actions.get(i).getAfterActionValue()).thenReturn((BigDecimal) v[0]));
        initValues.forEach((i, v) -> when(actions.get(i).getActionDate()).thenReturn((LocalDate) v[1]));
        initValues.forEach((i, v) -> when(actions.get(i).getActionType()).thenReturn((ActionType) v[2]));

        List<BigDecimal> excelGains = new ArrayList<>();
        for (Double doubleGain : new Double[]{34.62, 69.85, 108.14, 148.29}) {
            excelGains.add(new BigDecimal(doubleGain).setScale(2,RoundingMode.HALF_DOWN));
        }

        //when
        Map<LocalDate, BigDecimal> gain = financeCalcService.getGain(actions);

        //then
        for (int i = 0; i < 4; i++) {
            assertThat(gain.get(capitDates.get(i+1)), is(excelGains.get(i)));
        }
    }
}