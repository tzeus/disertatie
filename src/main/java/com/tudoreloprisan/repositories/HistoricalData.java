/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.repositories;

import java.sql.Timestamp;

import javax.persistence.*;

import lombok.Data;


@Data
@Entity
@Table(name = "historical_data")
public class HistoricalData {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private Float mid_h;
    private Float mid_l;
    private Float mid_o;
    private Float mid_c;
    private Float ask_l;
    private Float ask_o;
    private Float ask_c;
    private Float ask_h;
    private Float bid_o;
    private Float bid_c;
    private Float bid_l;
    private Float bid_h;
    private String instrument;
    private Timestamp timestamp;

}
