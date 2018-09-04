import { updateObject } from './../utility';
import allTrades from '../../assets/trades2.json';
import allTransactions from '../../assets/transactions.json';
import allOrders from '../../assets/orders2.json';
import candleData from '../../assets/candleData.json';
import * as actionTypes from '../actions/actionTypes';

let data = candleData.prices.map((price) => {
  return (
    data = {
      high: price.highPrice,
      low: price.lowPrice,
      close: price.closePrice,
      date: new Date(price.eventDate),
      volume: price.volume,
      open: price.openPrice,
    }
  );
})

const trades = allTrades.allTrades.slice(0);


const orders = allOrders.allOrders.slice(0);

const transactions = allTransactions.allTransactions.slice(0);


const initialState = {
  trade: {
    "tradeId": "48",
    "units": "-357",
    "side": "SHORT",
    "instrument": {
      "instrument": "GBP_USD",
      "pip": 0.0,
      "hash": 11864645
    },
    "tradeDate": "2018-08-29T23:38:14.000+03:00",
    "takeProfitPrice": 0.0,
    "executionPrice": 1.30311,
    "stopLoss": 0.0,
    "accountId": "101-004-9126938-001",
    "state": "OPEN",
    "realizedPL": "0.0000",
    "unrealizedPL": "3.1643",
    "financing": "0.0322",
    "initialMarginRequired": "13.2308",
    "marginUsed": "13.2504"


  },
  trades: trades,
  order: {

  },
  orders: orders,
  data: data,
  account: {
    "id": 0,
    "totalBalance": 100003.4314,
    "unrealisedPnl": -18.1575,
    "realisedPnl": 0.0, "marginUsed": 71.2056,
    "marginAvailable": 99914.0683,
    "netAssetValue": 99985.2739,
    "amountAvailableRatio": 0.999106399663002,
    "marginRate": 0.02,
    "openTrades": 3,
    "currency": "EUR",
    "accountId": "101-004-9126938-001",
    "hash": 956038545
  },
  stats: {

  },
  transactions: transactions
}

const setOrders = (state, action ) => {
  return updateObject(state, {
    orders: action.orders
  });
};

const setTrades = (state, action ) => {
  return updateObject(state, {
    trades: action.trades
  });
};

const loadTrade = (state, action) => {
  console.log('Entered loadTrade ' + action.trade.tradeId);

  return updateObject(state, { trade: action.trade })
};

const reducer = (state = initialState, action) => {
  switch (action.type) {
    case actionTypes.LOAD_TRADE: return loadTrade(state, action);
    case actionTypes.SET_ORDERS: return setOrders(state, action);
    case actionTypes.SET_TRADES: return setTrades(state, action);

    default:
      return state;

  }
}


export default reducer;