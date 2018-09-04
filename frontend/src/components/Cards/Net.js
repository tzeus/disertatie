import React from 'react';
import classes from './Net.css';

const net = (props) => {
  return(
    <div className={classes.Wrapper}>
      <div className={classes.LeftContent}>
        {props.text}
      </div>
      <div className={classes.RightContent}>
        {parseFloat(props.amount).toFixed(3)} {'\u20AC'}
      </div>
    </div>
  );
}

export default net;