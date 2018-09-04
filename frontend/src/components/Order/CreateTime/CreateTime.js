import React from 'react';
import classes from './CreateTime.css';

const createTime =(props) => {
  return(
    <div className={classes.CreateTime}>Created at: {new Date(props.createTime).toString()}</div>
  );
}

export default createTime;