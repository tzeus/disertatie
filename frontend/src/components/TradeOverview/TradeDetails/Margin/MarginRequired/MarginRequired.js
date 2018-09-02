import React from 'react';
import classes from './MarginRequired.css';

const marginRequired = (props) => {
  return (
    <div className={classes.MarginRequired}>Required Margin: {props.marginRequired} {'\u20AC'}</div>
  );
}

export default marginRequired;