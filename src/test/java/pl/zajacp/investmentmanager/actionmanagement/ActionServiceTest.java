package pl.zajacp.investmentmanager.actionmanagement;

import org.junit.BeforeClass;
import org.junit.Test;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionServiceTest {

    private static ActionService actionService = new ActionService(mock(ActionRepository.class));
    private static SavingsAccount savingsAccount;
    private static Investment investment;

    @BeforeClass
    public static void prepareProducts() {
        LocalDate openDate = LocalDate.parse("2018-03-01", DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate validityDate = LocalDate.parse("2018-09-10", DateTimeFormatter.ISO_LOCAL_DATE);
        savingsAccount = mock(SavingsAccount.class);
        investment = mock(Investment.class);

        for (FinanceProduct prod : new FinanceProduct[]{savingsAccount, investment}) {
            when(prod.getOpenDate()).thenReturn(openDate);
            when(prod.getCreated()).thenReturn(openDate.plusMonths(1));
            when(prod.getInterest()).thenReturn(new BigDecimal(0.035));
            when(prod.getValue()).thenReturn(new BigDecimal(1000));
        }

        when(investment.getMonthsValid()).thenReturn(12L);
        when(savingsAccount.getValidityDate()).thenReturn(validityDate);
        when(savingsAccount.getInterestAboveLimit()).thenReturn(new BigDecimal(0.005));
        when(savingsAccount.getValueLimit()).thenReturn(new BigDecimal(10000));
    }

    @Test
    public void shouldInvestmentValueWithReturn() {
        //given
        BigDecimal expectedReturn = new BigDecimal(1000 + (1000 * 0.035) * 0.81).setScale(2, RoundingMode.HALF_UP);
        //when
        BigDecimal returnValue = actionService.getInvestmentValueWithReturn(investment);
        //then
        assertThat(returnValue, is(expectedReturn));
    }

    @Test
    public void shouldGetCapitalizationDates() {
        //given
        LocalDate lastCapitalisationDate = LocalDate.parse("2018-09-30", DateTimeFormatter.ISO_LOCAL_DATE);
        //when
        List<LocalDate> dates = actionService.getCapitalizationDates(savingsAccount);
        //then
        assertThat(dates, hasItem(lastCapitalisationDate));
        assertThat(dates.size(), is(6));
    }

    @Test
    public void shouldCapitalizedValue() {
        //given
        BigDecimal expectedValue = new BigDecimal(1000 + (1000 * 0.035 * (30.0 / 365) * 0.81))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();
        //when
        BigDecimal CapitalizedValue = actionService.getCapitalizedValue(value, savingsAccount, date);
        //then
        assertThat(CapitalizedValue, is(expectedValue));
    }

    @Test
    public void shouldCapitalizedValueAtLimit() {
        //given
        when(savingsAccount.getValue()).thenReturn(new BigDecimal(9980));

        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();

        BigDecimal excelCalculation = new BigDecimal(10008.69);
        BigDecimal excelFloatError = excelCalculation.multiply(new BigDecimal(0.00003));
        //when

        BigDecimal valueCapitalizedAtLimit = actionService.getCapitalizedValue(value, savingsAccount, date);
        //then
        assertThat(valueCapitalizedAtLimit,is(closeTo(excelCalculation,excelFloatError)));
    }

    @Test
    public void shouldCapitalizedValueAtLimitLarge() {
        //given
        when(savingsAccount.getValue()).thenReturn(new BigDecimal(499000));
        when(savingsAccount.getValueLimit()).thenReturn(new BigDecimal(500000));
        when(savingsAccount.getInterest()).thenReturn(new BigDecimal(0.05));
        when(savingsAccount.getInterestAboveLimit()).thenReturn(new BigDecimal(0.002));

        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();

        BigDecimal excelCalculation = new BigDecimal(500235.43);
        BigDecimal excelFloatError = excelCalculation.multiply(new BigDecimal(0.00003));
        //when
        BigDecimal valueCapitalizedAtLimit = actionService.getCapitalizedValue(value, savingsAccount, date);
        //then
        assertThat(valueCapitalizedAtLimit,is(closeTo(excelCalculation,excelFloatError)));
    }


    @Test
    public void shouldPartialCapitalizedValue() {
        //given
        BigDecimal firstHalfExpectedValue = new BigDecimal(1000 + (1000 * 0.035 * (30.0 / 365) * 0.81 * 10.0 / 30))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal secondHalfExpectedValue = new BigDecimal(1000 + (1000 * 0.035 * (30.0 / 365) * 0.81 * (1 - 10.0 / 30)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal value = savingsAccount.getValue();
        LocalDate date = savingsAccount.getValidityDate();
        //when
        BigDecimal firstHalf = actionService.getPartialCapitalizedValue(value, savingsAccount, date, true);
        BigDecimal secondHalf = actionService.getPartialCapitalizedValue(value, savingsAccount, date, false);
        //then
        assertThat(firstHalf, is(firstHalfExpectedValue));
        assertThat(secondHalf, is(secondHalfExpectedValue));
    }

}