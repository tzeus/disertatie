import React, { Component } from 'react';
import classes from './TradeOverview.css'
import Auxiliary from './../../hoc/Auxiliary';
import TradeList from './TradeList/TradeList';
import Trade from './Trade/Trade';
import TradeDetails from './TradeDetails/TradeDetails';
import allTrades from '../../assets/trades.json';
import { connect } from 'react-redux';
class TradeOverview extends Component {


  render() {

    return (
      <Auxiliary>
        <div className={classes.Wrapper}>
          <div className={classes.TradeList}>
            <TradeList />
          </div>

          <div className={classes.TradeDetails}>
            <TradeDetails trades={allTrades} />
          </div>
          <div className={classes.Trade} >
            <Trade />
          </div>
        </div>
      </Auxiliary>
    );

  }
}

const mapStateToProps = (state) => {
  return {
    trades: state.trades,
    trade: state.trade
  };
}

// const mapDispatchToProps = dispatch => {
// return {
//   // onTradeClick: () => dispatch({type: 'LOAD_TRADE'})
// };
// }


export default connect(mapStateToProps)(TradeOverview);
