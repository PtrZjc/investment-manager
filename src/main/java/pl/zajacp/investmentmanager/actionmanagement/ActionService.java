package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.data.FinanceCalcService;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.Investment;
import pl.zajacp.investmentmanager.products.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Service
@Transactional
public class ActionService {

    private final ActionRepository actionRepository;
    private final FinanceCalcService financeCalcService;

    public ActionService(ActionRepository actionRepository, FinanceCalcService financeCalcService) {
        this.actionRepository = actionRepository;
        this.financeCalcService = financeCalcService;
    }

    public Action createTheSameMonthLater(Action baseAction) {
        Action action = new Action();
        action.setBalanceChange(baseAction.getBalanceChange());
        action.setProduct(baseAction.getProduct());
        action.setActionDate(baseAction.getActionDate().plusMonths(1));
        action.setActionType(baseAction.getActionType());
        action.setNotes(baseAction.getNotes());
        return action;
    }

    public void initializeInvestmentActions(Investment product) {
        setOpenCloseActions(product);
    }

    public void initializeSavingsAccountActions(SavingsAccount product) {

        /*
         * Initializes capitalization actions for the saving accounts. Generates 12 capitalization actions,
         * one at last day of month, starting at product creation month.
         * */

        setOpenCloseActions(product);

        BigDecimal lastValue = product.getValue();
        List<LocalDate> capitalizationDates = financeCalcService.getCapitalizationDates(product);
        LocalDate startCalcDate =product.getOpenDate();
        LocalDate endDate = product.getValidityDate();
        int endDateIndex = getEndDateIndex(capitalizationDates, endDate);
        /*
         * The value of each subsequent generated capitalization is dependent on the previous one value.
         * First month needs to be calculated proportionally.
         * */
        for (int i = 0; i < capitalizationDates.size(); i++) {
            Action capitalizationAction = new Action();
            capitalizationAction.setProduct(product);
            capitalizationAction.setActionType(ActionType.CAPITALIZATION);
            capitalizationAction.setActionDate(capitalizationDates.get(i));

            BigDecimal capitalizationChange;

            if (i == 0) {
                capitalizationChange = financeCalcService.
                        getFirstMonthCapitalization(lastValue, product);
            } else if (i <= endDateIndex) {
                capitalizationChange = financeCalcService.
                        getMonthCapitalization(lastValue, product, capitalizationDates.get(i));
            } else {
                capitalizationChange = financeCalcService.
                        getAfterPromotionCapitalization(lastValue, product, capitalizationDates.get(i));
            }
            capitalizationAction.
                    setAfterActionValue(lastValue.add(capitalizationChange).setScale(2, RoundingMode.DOWN));
            actionRepository.save(capitalizationAction);
            lastValue = capitalizationAction.getAfterActionValue();

        }

    }

    private int getEndDateIndex(List<LocalDate> capitalizationDates, LocalDate endDate) {
        for (int i = 0; i < capitalizationDates.size() - 1; i++) {
            if (endDate.isBefore(capitalizationDates.get(i))) {
                return i;
            }
        }
        throw new NullPointerException();
    }

    public void setOpenCloseActions(FinanceProduct product) {

        Action open = new Action();
        open.setActionType(ActionType.PRODUCT_OPEN);
        open.setAfterActionValue(product.getValue());
        open.setActionDate(product.getOpenDate());
        open.setProduct(product);

        Action close = new Action();
        close.setActionType(ActionType.PRODUCT_CLOSE);

        if (product instanceof Investment) {
            close.setAfterActionValue(financeCalcService.getInvestmentValueWithReturn((Investment) product));
            close.setActionDate(product.getOpenDate().plusMonths(((Investment) product).getMonthsValid()));
        } else if (product instanceof SavingsAccount) {
            close.setActionDate(((SavingsAccount) product).getValidityDate());
        }
        close.setProduct(product);

        actionRepository.save(open);
        actionRepository.save(close);
    }


    public void save(Action action) {
        actionRepository.save(action);
    }

    //TODO choosing yestarday when entering payment gets Nullpointerexception
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
                    .filter(x->x.getActionType()==ActionType.PRODUCT_CLOSE)
                    .map(Action::getActionDate)
                    .findFirst()
                    .orElseThrow(NullPointerException::new);

            while (action.getActionDate().isBefore(latestDate.minusMonths(1))) {
                action = createTheSameMonthLater(action);
                actions.add(action);
            }
        }

        actionRepository.saveAll(actions);
        product.getActions().addAll(actions);
    }



    public void recalculateCapitalizations(SavingsAccount product){
        sortActionsByDate(product.getActions());
        List<Action> actions = financeCalcService.recalculateCapitalizations(product, true);
        actionRepository.saveAll(actions);
    }

    public Action findById(Long id) {
        return actionRepository.findById(id).orElseThrow(NullPointerException::new);
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
                        x.getActionDate().getMonth().plus(1) == actionDto.getActionDate().getMonth();

        BigDecimal capValue = product.getActions().stream()
                .filter(previousMonthCap)
                .min(Comparator.comparing(Action::getActionDate))
                .map(Action::getAfterActionValue)
                .orElseThrow(NullPointerException::new);

        Predicate<Action> widthdrawFilter = a-> a.getActionType()==ActionType.BALANCE_CHANGE
                && a.getBalanceChange().compareTo(BigDecimal.ZERO)<0;

        BigDecimal widthdraws = product.getActions().stream()
                .filter(widthdrawFilter)
                .map(Action::getBalanceChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        capValue = capValue.add(widthdraws);

        return actionDto.getAmount().compareTo(capValue) < 0;
    }

    public void sortActionsByDate(List<Action> actions) {
        actions.sort(Comparator
                .comparing(Action::getActionDate)
                .thenComparing(a -> a.getActionType().toString()));
    }
}