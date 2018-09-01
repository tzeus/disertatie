import React from 'react';
import classes from './CreateTime.css';

const createTime =(props) => {
  return(
    <div className={classes.CreateTime}>Created at: {props.createTime}</div>
  );
}

export default createTime;