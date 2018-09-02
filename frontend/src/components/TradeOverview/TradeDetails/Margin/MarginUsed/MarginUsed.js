import React from 'react';
import classes from './MarginUsed.css';

const marginUsed = (props) => {
  return (
    <div className={classes.MarginUsed}>Used Margin: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{props.marginUsed} {'\u20AC'}</div>
  );
}

export default marginUsed;