import React, {Component} from 'react';
import classes from './TradeList.css';
import { connect } from 'react-redux';
import * as actions from '../../../store/actions/action';


class TradeList extends Component{


    componentDidMount() {
     
    }

  


render(){

  let tradeList = this.props.trades.map((trade) => {
    return (
      <li key={trade.tradeId} style={{ backgroundColor: trade.unrealizedPL >= 0 ? '#5C9210' : '#944317' }} onClick={()=>this.props.onTradeLoad(trade)}>
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

const mapStateToProps = (state) => {
  return {
    trade: state.trade,
    trades: state.trades
  };
}


const mapDispachToProps = dispatch => {
  return {
  onTradeLoad: (trade) => dispatch(actions.loadTrade(trade))
  }
}

export default connect(mapStateToProps, mapDispachToProps)(TradeList);
