import React from 'react';
import classes from './Statistics.css';

const statistics = (props) => {
  return (

    <div className={classes.Statistics}>
      <div className={classes.left}>
        Pending orders: <br />
        Filled orders:<br />
        Cancelled orders:<br />
        Open trades:<br />
        Closed trades:<br />
        Best trade:<br />
        Worst trade:<br />
        Best pair:<br />
        Worst pair:<br />
      </div>
      <div className={classes.right}>
        <strong>{props.pendingOrders}</strong><br />
        <strong>{props.filledOrders} </strong><br />
        <strong>{props.cancelledOrders}</strong><br />
        <strong>{props.openTrades}</strong><br />
        <strong>{props.closedTrades}</strong><br />
        <strong>{props.highestTrade} {'\u20AC'}</strong><br />
        <strong>{props.lowestTrade} {'\u20AC'}</strong><br />
        <strong>{props.bestPair}</strong><br />
        <strong>{props.worstPair}</strong><br />
      </div>
    </div>


  );
}

export default statistics;
