import React from 'react';
import classes from './RealizedUnrealizedPL.css'

const realizedUnrealizedPL = (props) => {
  return (
    <div className={classes.RealizedUnrealizedPL}> 
      RealizedPL: &nbsp;&nbsp; {props.realizedPL} {'\u20AC'}<br />
      UnrealizedPL: {props.unrealizedPL} {'\u20AC'}
    </div>
  );
}

export default realizedUnrealizedPL;