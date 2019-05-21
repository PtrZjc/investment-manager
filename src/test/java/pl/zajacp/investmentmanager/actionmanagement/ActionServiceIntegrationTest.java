package pl.zajacp.investmentmanager.actionmanagement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ActionServiceIntegrationTest {

    //TODO w weekend set up integration test

    @MockBean
    ActionRepository actionRepository;


    @Test
    public void shouldDivideActionsBeforeRecalculate() {
        //given
        ActionServiceTest.prepareActions();
        ActionServiceTest.prepareProducts();

        //when

        //ActionServiceTest.actionService.recalculateCapitalizations(savingsAccount, false, actions);
        //then
    }
}
