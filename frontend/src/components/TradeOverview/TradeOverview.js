import React, {Component} from 'react';
import classes from './TradeOverview.css'
import Auxiliary from './../../hoc/Auxiliary';
import TradeList from './TradeList/TradeList';
// import Trade from './Trade/Trade';
import TradeDetails from './TradeDetails/TradeDetails';
import allTrades from '../../assets/trades.json';

class TradeOverview extends Component{


  render(){

    return(
      <Auxiliary>
        <div className={classes.Wrapper}>
          <TradeList />
           <TradeDetails trades={allTrades}/>
          {/*<Trade /> */}
        </div>
      </Auxiliary>
    );

  }
}

export default TradeOverview;
