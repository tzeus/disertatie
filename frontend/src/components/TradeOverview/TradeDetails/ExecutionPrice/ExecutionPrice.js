import React from 'react';
import classes from './ExecutionPrice.css';

const executionPrice = (props) => {
  return (
    <div className={classes.ExecutionPrice}>Execution price: {props.executionPrice}</div>
  );
}

export default executionPrice;
