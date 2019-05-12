package pl.zajacp.investmentmanager.actionmanagement;

import org.junit.Test;
import pl.zajacp.investmentmanager.products.investment.Investment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionServiceTest {

    private final static LocalDate openDate = LocalDate.parse("2018-05-01", DateTimeFormatter.ISO_LOCAL_DATE);
    private final static LocalDate validityDate = LocalDate.parse("2020-07-13", DateTimeFormatter.ISO_LOCAL_DATE);
    private final static ActionService actionService = new ActionService(mock(ActionRepository.class));

    @Test
    public void shouldInvestmentValueWithReturn() {
        Investment product = mock(Investment.class);
        when(product.getOpenDate()).thenReturn(openDate);
        when(product.getMonthsValid()).thenReturn(12L);
        when(product.getValue()).thenReturn(new BigDecimal(1000));
        when(product.getInterest()).thenReturn(new BigDecimal(0.035));
        assertThat(actionService.investmentValueWithReturn(product),
                is(new BigDecimal(1000+(1000*0.035)*0.81).setScale(2, RoundingMode.HALF_UP)));
    }
}