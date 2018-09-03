import React, { Component } from 'react';
import classes from './account.css';
import Card from './../Cards/Card1';
import { connect } from 'react-redux';
import * as actions from '../../store/actions/action';

class Account extends Component {

  render() {
    return (
      <div className={classes.Account}>
        <div className={classes.left}>
          Account ID:<br />
          Total balance:<br />
          Currency:<br />
          Open Trades:<br />
          Margin rate:<br />
          Margin available:<br />
          Margin used:<br />
          Amount available ratio:<br />
          Net asset value:<br />
          Realized PnL:<br />
          Unrealized PnL:<br />
        </div>
        <div className={classes.right}>

          <strong>{this.props.account.accountId}</strong><br />
          <strong>{this.props.account.totalBalance}</strong><br />
          <strong>{this.props.account.currency}</strong><br />
          <strong>{this.props.account.openTrades} </strong><br />
          <strong>{this.props.account.marginRate}  </strong><br />
          <strong>{this.props.account.marginAvailable}  </strong><br />
          <strong>{this.props.account.marginUsed}  </strong><br />
          <strong>{this.props.account.amountAvailableRatio}  </strong><br />
          <strong>{this.props.account.netAssetValue}  </strong><br />
          <strong>{this.props.account.realisedPnl}  </strong><br />
          <strong>{this.props.account.unrealisedPnl}  </strong><br />

        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    account: state.account,
    stats: state.stats,
    transactions: state.transactions
  }
}



export default connect(mapStateToProps)(Account);