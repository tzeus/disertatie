/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.repositories;

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
    private Double mid_h;
    private Double mid_l;
    private Double mid_o;
    private Double mid_c;
    private Double ask_l;
    private Double ask_o;
    private Double ask_c;
    private Double ask_h;
    private Double bid_o;
    private Double bid_c;
    private Double bid_l;
    private Double bid_h;
    private String instrument;
    private Long timestamp;
    private Long volume;
    private String granularity;

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Constructors 
    //~ ----------------------------------------------------------------------------------------------------------------

    public HistoricalData(Double mid_h, Double mid_l, Double mid_o, Double mid_c, Double ask_l, Double ask_o, Double ask_c, Double ask_h, Double bid_o, Double bid_c, Double bid_l, Double bid_h, String instrument,
        Long timestamp, Long volume, String granularity) {
        this.mid_h = mid_h;
        this.mid_l = mid_l;
        this.mid_o = mid_o;
        this.mid_c = mid_c;
        this.ask_l = ask_l;
        this.ask_o = ask_o;
        this.ask_c = ask_c;
        this.ask_h = ask_h;
        this.bid_o = bid_o;
        this.bid_c = bid_c;
        this.bid_l = bid_l;
        this.bid_h = bid_h;
        this.instrument = instrument;
        this.timestamp = timestamp;
        this.volume = volume;
        this.granularity = granularity;
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    public Long getTimestamp() {
        return timestamp;
    }
}
