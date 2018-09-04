import * as actionTypes from './actionTypes';
import axios from '../../axios-order';

export const loadTrade = (trade) => {

  return {
    type: actionTypes.LOAD_TRADE,
    trade: trade
  };
};



export const setOrders = (orders) => {
  return {
    type: actionTypes.SET_ORDERS,
    orders: orders
  }
};

export const initOrders = () => {
  return dispatch => {
    axios.get('/getOrders')
      .then(response => {
        dispatch(setOrders(response.data));
      }).catch(error => {
        
      });
  }
};

export const setTrades = (trades) => {
  return {
    type: actionTypes.SET_TRADES,
    trades: trades
  }
};

export const initTrades = () => {
  return dispatch => {
    axios.get('/getTrades')
      .then(response => {
        dispatch(setTrades(response.data));
      }).catch(error => {
        
      });
  }
};