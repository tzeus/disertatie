import React from 'react';
import classes from './TradeID.css'
const tradeId = (props) =>{
  return (
    <div className={classes.TradeID}>Trade Id: {props.tradeId}</div>
  );
}

export default tradeId;