import * as actionTypes from './actionTypes';

export const loadTrade = (trade) => {

  return {
    type: actionTypes.LOAD_TRADE,
    trade: trade
  };
};