import  React from 'react';
import classes from './TradeState.css';
const tradeState = (props) => {
  return(
    <div className={classes.TradeState}>
    TRADE {props.state}<br />
    </div>
  );
}

export default tradeState;