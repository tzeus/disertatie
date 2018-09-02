import React from 'react';
import bull from '../../../assets/images/position/bull.png';
import bear from '../../../assets/images/position/bear.png';
import classes from './Side.css';

const side = (props) => {

  const direction = (side) => {
    if (typeof side === 'undefined') {
      return;
    }
    return side === 'LONG' ? bull : bear;
  }

  return (
    <div className={classes.Side}>
      <img width={props.width} height={props.height} src={direction(props.side)} alt={props.side}></img>
    </div>
  );

}

export default side;

