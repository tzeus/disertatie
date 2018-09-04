import React, { Component } from 'react';
import { connect } from 'react-redux';
import Button from '../../components/UI/Button/Button';
import classes from './PlaceOrder.css';


class PlaceOrder extends Component {
  
  
  orderHanlder = () => {
    
  }
  
  render() {



    return (
      <div>
        <h4>Place a new order</h4>
        <form>

          <input className={classes.Input} id="price" name="price" type="text" placeholder="@enterTrade" required="" />
          <input className={classes.Input} id="tp" name="tp" type="text" placeholder="@takeProfit" required="" />
          <input className={classes.Input} id="sl" name="sl" type="text" placeholder="@stopLoss" required="" />


          <div>
            <select id="currency" name="currency" class="form-control">
              <option value="EUR_USD">EUR_USD</option>
              <option value="EUR_CHF">EUR_CHF</option>
              <option value="USD_JPY">USD_JPY</option>
              <option value="USD_CHF">USD_CHF</option>
              <option value="GBP_USD">GBP_USD</option>
              <option value="EUR_GBP">EUR_GBP</option>
            </select>
          </div>
          <Button btnType="Success" clicked={this.orderHanlder}>Place order</Button>
        </form>
      </div>
    );
  }
}


const mapStateToProps = (state) => {
  return {

  };
}
export default connect(mapStateToProps)(PlaceOrder);
