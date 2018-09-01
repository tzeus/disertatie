import React from 'react';
import classes from './TPSL.css'
const tpsl = (props) => {
return (
  <div className={classes.TPSL}>
  TP: {props.takeProfit}<br />
  SL: {props.stopLoss}
  </div>
);
}
export default tpsl;
