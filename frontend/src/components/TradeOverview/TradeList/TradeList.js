import React, {Component} from 'react';
import classes from './TradeList.css';
import allTrades from '../../../assets/trades.json';


class TradeList extends Component{

  state = {
    trades:[]
  }

    componentDidMount() {
      let trades = allTrades.allTrades.slice(0);
      this.setState({ trades: trades });
    }

    loadClickHandler = (tradeId) => {
      console.log('Clicked trade ' + tradeId);
    }
  


render(){

  let tradeList = this.state.trades.map((trade) => {
    return (
      <li key={trade.tradeId} style={{ backgroundColor: trade.state === 'OPEN' ? '#5C9210' : '#944317' }} onClick={() => this.loadClickHandler(trade.tradeId)}>
        Trade ID: {trade.tradeId}
      </li>
    )
  });

  return (
      <div className={classes.NavigationItems}>
            <ul>
              <li>Recent trades</li>
              {tradeList}
            </ul>
    </div>
  );
};
}

export default TradeList;
