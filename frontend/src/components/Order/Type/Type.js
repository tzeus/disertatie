import React from 'react';
import classes from './Type.css';

const Type = (props) => {
  return (
    <div className={classes.Type}>
      {props.type === 'LIMIT' ? 'LIMIT ORDER' : 'MARKET ORDER'}
    </div>

  );
}

export default Type;