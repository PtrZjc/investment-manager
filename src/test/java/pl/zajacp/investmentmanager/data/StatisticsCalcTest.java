package pl.zajacp.investmentmanager.data;

import pl.zajacp.investmentmanager.actionmanagement.Action;
import pl.zajacp.investmentmanager.actionmanagement.ActionType;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.SavingsAccount;
import pl.zajacp.investmentmanager.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticsCalcTest {

    private static StatisticsService statisticsService = new StatisticsService();

    public void getProductGainOfLastMonths() {

        //given
        int size = 5;
        List<Action> actions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            actions.add(mock(Action.class));
            when(actions.get(i).getActionType()).thenReturn(ActionType.CAPITALIZATION);
            when(actions.get(i).getAfterActionValue()).thenReturn(BigDecimal.valueOf(100*(i+1)));
            when(actions.get(i).getActionDate()).thenReturn(YearMonth.now().minusMonths(size-i).atEndOfMonth());
        }
        actions.add(mock(Action.class));
        when(actions.get(size).getActionType()).thenReturn(ActionType.BALANCE_CHANGE);
        when(actions.get(size).getActionDate()).thenReturn(LocalDate.now());
        when(actions.get(size).getActionDate()).thenReturn(LocalDate.now());

        SavingsAccount product = mock(SavingsAccount.class);
        when(product.getActions()).thenReturn(actions);

        User user = mock(User.class);
        List<FinanceProduct> productList = null;
        when(user.getProducts()).thenReturn(productList);

        //List<FinanceProduct> df =Arrays.asList(product);

        //when
        BigDecimal thisMonthValue = statisticsService.getTotalUserValueInMonth(user,1);

        int x = 1;
        //then
//        assertThat(oneMonthGain, is(BigDecimal.valueOf(100)));
//        assertThat(threeMonthsGain, is(BigDecimal.valueOf(300)));
//        assertThat(twelveMonthsGain, is(BigDecimal.valueOf(1200)));
    }
}