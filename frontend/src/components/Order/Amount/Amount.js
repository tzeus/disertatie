import React from 'react';
import classes from './Amount.css';

const amount = (props) => {
  const units = typeof props.amount  === 'undefined' ? 0 : props.amount;
  
  return(
    <div className={classes.Amount}>Amount: <strong>{Math.abs(units)}</strong></div>
  );
}

export default amount;