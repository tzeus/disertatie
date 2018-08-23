/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.account;

public enum OandaJsonKeys {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Enum constants 
    //~ ----------------------------------------------------------------------------------------------------------------

    ACCOUNT("account"), ACCOUNTS("accounts"), ACCOUNT_ID("id"), ACCOUNT_CURRENCY("currency"), MARGIN_RATE("marginRate"), MARGIN_USED("marginUsed"), MARGIN_AVAIL("marginAvailable"), BALANCE("balance"),
    UNREALIZED_PL("unrealizedPL"), REALIZED_PL("realizedPL"), OPEN_TRADES("openTradeCount"), INSTRUMENTS("instruments"), INSTRUMENT("instrument"), INTEREST_RATE("interestRate"), DISCONNECT("disconnect"), PIP("pipLocation"),
    BID("bid"), ASK("ask"), HEARTBEAT("heartbeat"), CANDLES("candles"), CODE("code"), OPEN("o"), HIGH("h"), LOW("l"), CLOSE("c"), TIME("time"), TICK("tick"), PRICES("prices"), TRADES("trades"), TRADE_ID("tradeId"),
    PRICE("price"), AVG_PRICE("avgPrice"), ID("id"), STOP_LOSS_ON_FILL("stopLossOnFill"), TAKE_PROFIT_ON_FILL("takeProfitOnFill"), UNITS("units"), SIDE("side"), TYPE("type"), ORDERS("orders"), ORDER_ID("orderId"),
    POSITIONS("positions"), EXPIRY("expiry"), TRADE_OPENED("tradeOpened"), ORDER_OPENED("orderOpened"), TRANSACTION("transaction"), PL("pl"), INTEREST("interest"), ACCOUNT_BALANCE("accountBalance"), NAME("name"),
    BIDS("bids"), ASKS("asks"), LIQUIDITY("liquidity"), MIDPOINT("mid"), ORDER("order"), ORDER_CREATE_TRANSACTION("orderCreateTransaction"), OPEN_TIME("openTime"), CURRENT_UNITS("currentUnits"),
    TAKE_PROFIT_ORDER("takeProfitOrder"), STOP_LOSS_ORDER("stopLossOrder "), TAKE_PROFIT("takeProfit"), STOP_LOSS("stopLoss");

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    private String value;

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Constructors 
    //~ ----------------------------------------------------------------------------------------------------------------

    OandaJsonKeys(String value) {
        this.value = value;
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    public String value() {
        return value;
    }

}
