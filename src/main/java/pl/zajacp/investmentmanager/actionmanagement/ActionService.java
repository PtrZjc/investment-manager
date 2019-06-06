package pl.zajacp.investmentmanager.actionmanagement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zajacp.investmentmanager.products.FinanceProduct;
import pl.zajacp.investmentmanager.products.investment.Investment;
import pl.zajacp.investmentmanager.products.savings.SavingsAccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
         * Initializes capitalization actions for the saving accounts. Generated are up to 12 capitalization actions,
         * one at last day of month, starting at product creation month up to validity date or 12 (in case validity
         * date is absent).
         * */

        setOpenCloseActions(product);

        BigDecimal lastValue = product.getValue();
        List<LocalDate> capitalizationDates = financeCalcService.getCapitalizationDates(product);
        LocalDate startCalcDate = product.getOpenDate().isBefore(LocalDate.now().withDayOfMonth(1)) ?
                product.getOpenDate() :
                LocalDate.now().withDayOfMonth(1);
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
                capitalizationChange = startCalcDate.getMonth() == product.getCreated().getMonth() ?
                        financeCalcService.
                                getPartialCapitalizedValue(lastValue, product, startCalcDate, false) :
                        financeCalcService.
                                getCapitalization(lastValue, product, capitalizationDates.get(i));
            } else if (i < capitalizationDates.size() - 1) {
                //all months but last
                capitalizationChange = financeCalcService.
                        getCapitalization(lastValue, product, capitalizationDates.get(i));
            } else {
                //last month
                capitalizationChange = endDate != null ?
                        financeCalcService.
                                getPartialCapitalizedValue(lastValue, product, endDate, true) :
                        financeCalcService.
                                getCapitalization(lastValue, product, capitalizationDates.get(i));
            }

            capitalizationAction.
                    setAfterActionValue(lastValue.add(capitalizationChange).setScale(2, RoundingMode.HALF_UP));

            lastValue = capitalizationAction.getAfterActionValue();

            actionRepository.save(capitalizationAction);
        }
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
                    .max(Comparator.comparing(Action::getActionDate))
                    .map(Action::getActionDate)
                    .orElseThrow(NullPointerException::new);

            while (action.getActionDate().isBefore(latestDate.minusMonths(1))) {
                action = createTheSameMonthLater(action);
                actions.add(action);
            }
        }

        actionRepository.saveAll(actions);
        product.getActions().addAll(actions);
    }

    public Action findById(Long id){
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

        return actionDto.getAmount().compareTo(capValue) < 0;
    }

    public List<Action> getChartActions(SavingsAccount product) {
        return product.getActions().stream()
                .filter(x -> Boolean.FALSE.equals(x.getIsDone()))
                .collect(Collectors.toList());
    }

    public void sortActionsByDate(List<Action> actions) {
        actions.sort(Comparator
                .comparing(Action::getActionDate)
                .thenComparing(a -> a.getActionType().toString()));
    }
}