import React, { Component } from 'react';
import classes from './TradeList.css';
import { connect } from 'react-redux';
import * as actions from '../../../store/actions/action';
import Auxiliary from './../../../hoc/Auxiliary';
import Axios from 'axios';


class TradeList extends Component {


  componentDidMount() {
this.props.onInitTrades();
  }



  render() {

    const getTradeProfitColor = (trade) => {
      let tradeProfit = trade.unrealizedPL !== 0 ? trade.unrealizedPL : trade.realizedPL;
      if (tradeProfit < 0) {
        return '#5C9210';
      } else if (tradeProfit === 0) {
        return 'gray';
      } else {
        return '#944317';
      }
    }

    let tradeList = this.props.trades.map((trade) => {
      return (
        <li key={trade.tradeId} style={{ backgroundColor: getTradeProfitColor(trade) }} onClick={() => this.props.onTradeLoad(trade)}>
          Trade ID: {trade.tradeId}
        </li>
      )
    });

    return (
      <Auxiliary>
        <div className={classes.Wrapper}>
          <div className={classes.NavigationItems}>
            <ul>
              <li>Recent trades</li>
              {tradeList}
            </ul>
          </div>
        </div>
      </Auxiliary>
    );
  };
}

const mapStateToProps = (state) => {
  return {
    trade: state.trade,
    trades: state.trades
  };
}


const mapDispachToProps = dispatch => {
  return {
    onTradeLoad: (trade) => dispatch(actions.loadTrade(trade)),
    onInitTrades: () => {
      dispatch(actions.initTrades())
    }
  }
}

export default connect(mapStateToProps, mapDispachToProps)(TradeList, Axios);
