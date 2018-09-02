import React from 'react';
import classes from './Financing.css';

const financing = (props) => {
  return (
    <div className={classes.Financing}>Financing: 
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    {props.financing}  {'\u20AC'}</div>
  );
}

export default financing;