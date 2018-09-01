import  React from 'react';
import classes from './OrderState.css';
const orderState = (props) => {
  return(
    <div className={classes.OrderState}>
    ORDER {props.state}<br />
    Filling transaction Id: {props.fillingTransactionID}
    </div>
  );
}

export default orderState;