import React from 'react';
import classes from './OrderID.css'
const orderId = (props) =>{
  return (
    <div className={classes.OrderID}>Order Id: {props.orderId}</div>
  );
}

export default orderId;