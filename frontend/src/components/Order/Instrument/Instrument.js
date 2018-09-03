import React from 'react';
import classes from './Instrument.css';
import USD from '../../../assets/images/currency/usd.png';
import CHF from '../../../assets/images/currency/chf.png';
import EUR from '../../../assets/images/currency/eur.png';
import JPY from '../../../assets/images/currency/jpy.png';
import GBP from '../../../assets/images/currency/gbp.png';



const instrument = (props) => {

  const getCurrencyImage = (currency) => {
    switch (currency) {
      case 'USD':
        return USD;
      case 'EUR':
        return EUR;
      case 'CHF':
        return CHF;
      case 'JPY':
        return JPY;
      case 'GBP':
        return GBP;
      default:
        console.log('Error: wrong currency');

    }
  };

  const firstCurrency = (instrument) => {
    if (typeof instrument === 'undefined') {
      return '';
    }
    return instrument.substring(0, 3);
  };
  const secondCurrency = (instrument) => {
    if (typeof instrument === 'undefined') {
      return '';
    }
    return instrument.substring(4, 7);
  };




  return (
    <div className={classes.Instrument}>
      <img width={props.width} height={props.height} src={getCurrencyImage(firstCurrency(props.instrument.instrument))}
        alt={firstCurrency(props.instrument.instrument)}></img>
      <img width={props.width} height={props.height} src={getCurrencyImage(secondCurrency(props.instrument.instrument))}
        alt={secondCurrency(props.instrument.instrument)} ></img>
    </div>
  );
};

export default instrument;