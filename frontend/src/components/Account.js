import React, {Component} from 'react';
import classes from './account.css';

class Account extends Component {
  state = {
    "totalBalance":0,
    "unrealisedPnl":0.0,
    "realisedPnl":0.0,
    "marginUsed":0.0,
    "marginAvailable":0,
    "netAssetValue":0,
    "amountAvailableRatio":0,
    "marginRate":0.0,
    "openTrades":0,
    "currency":"EUR",
    "accountId":"101-004-9126938-001",
    "hash":0
  }
  render(){
    return(
      <div className={classes.Account}>
          <label style={{float: "left"}} style={{float: "left"}}>Account ID: <strong>{this.state.accountId}</strong>  </label><br />
          <label style={{float: "left"}}>Total balance: <strong>{this.state.totalBalance}</strong>  </label><br />
          <label style={{float: "left"}}>Currency: <strong>{this.state.currency}</strong>  </label><br />
          <label style={{float: "left"}}>Open trades: <strong>{this.state.openTrades} </strong>  </label><br />
          <label style={{float: "left"}}>Margin rate: <strong>{this.state.marginRate}  </strong> </label><br />
          <label style={{float: "left"}}>Margin available: <strong>{this.state.marginAvailable}  </strong> </label><br />
          <label style={{float: "left"}}>Margin used: <strong>{this.state.marginUsed}  </strong> </label><br />
          <label style={{float: "left"}}>Amount available ratio: <strong>{this.state.amountAvailableRatio}  </strong> </label><br />
          <label style={{float: "left"}}>Net asset value: <strong>{this.state.netAssetValue}  </strong> </label><br />
          <label style={{float: "left"}}>Realized PnL: <strong>{this.state.realisedPnl}  </strong> </label><br />
          <label style={{float: "left"}}>Unrealized PnL: <strong>{this.state.unrealisedPnl}  </strong> </label><br />

      </div>
    );
  }
}

export default Account;