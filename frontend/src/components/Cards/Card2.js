import React from 'react';
import classes from './Card2.css'

const card2 = (props) => (
  <div className={classes.Wrapper}>
    <div className={classes.LeftContent}>
    STATISTICS
    </div>
    <div className={classes.RightContent}>
      {props.text}
    </div>
  </div>
)

export default card2;