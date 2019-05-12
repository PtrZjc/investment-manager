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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionServiceTest {

    private static ActionService actionService = new ActionService(mock(ActionRepository.class));
    private static SavingsAccount savingsAccount;
    private static Investment investment;

    @BeforeClass
    public static void prepareProducts() {
        LocalDate openDate = LocalDate.parse("2018-03-01", DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate validityDate = LocalDate.parse("2018-09-15", DateTimeFormatter.ISO_LOCAL_DATE);
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
    }


    @Test
    public void shouldInvestmentValueWithReturn() {
        assertThat(actionService.investmentValueWithReturn(investment),
                is(new BigDecimal(1000 + (1000 * 0.035) * 0.81).setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    public void shouldGetCapitalizationDates() {
        List<LocalDate> dates = actionService.getCapitalizationDates(savingsAccount);
        LocalDate lastCapitalisationDate = LocalDate.parse("2018-09-30", DateTimeFormatter.ISO_LOCAL_DATE);
        assertThat(dates, hasItem(lastCapitalisationDate));
        assertThat(dates.size(), is(6));
    }

    @Test
    public void magicalPlayground() {
    }


}