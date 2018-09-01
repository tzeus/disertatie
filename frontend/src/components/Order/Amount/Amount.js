import React from 'react';
import classes from './Amount.css';

const Amount = (props) => {
  return(
    <div className={classes.Amount}>Amount: <strong>{Math.abs(props.amount)}</strong></div>
  );
}

export default Amount;