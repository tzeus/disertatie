import React from 'react';
import card1 from '../../assets/images/card1.png';
import classes from './Card1.css'

const card = () => (
  <div className={classes.Wrapper}>
    <div className={classes.LeftContent}>
      <img src={card1} alt="card1"></img>
    </div>
    <div className={classes.RightContent}>
    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
    Nullam placerat mi nisi, sit amet tincidunt urna molestie in.
    </div>
  </div>
)

export default card;