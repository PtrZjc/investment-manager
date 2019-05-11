package pl.zajacp.investmentmanager.actionmanagement;

public enum ActionType {
    PRODUCT_OPEN,
    PRODUCT_CLOSE,
    CAPITALIZATION,
    BALANCE_CORRECTION,
    PAYMENT,
    WIDTHDRAWAL,
}

//balance correction musi tworzyć akcję z różnicą salda do aktualnej wartości i pod koniec miesiąca musi być rozliczona.