import React from 'react';
import fxTradeLogo from '../../assets/images/logo.jpg';
import classes from './Logo.css';


const logo = (props) => (
    <div className={classes.Logo} style={{height: props.height}}>
        <img src={fxTradeLogo} alt="BurgerLogo" />
    </div>
);

export default logo;