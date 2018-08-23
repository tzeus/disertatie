/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package email;

import java.util.Map;

import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import tradingAPI.account.TradingConfig;

import tradingAPI.events.Event;
import tradingAPI.events.EventPayLoad;


public class EventEmailNotifier<T> {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(EventEmailNotifier.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Autowired
    JavaMailSender mailSender;
    @Resource
    Map<Event, EmailContentGenerator<T>> eventEmailContentGeneratorMap;
    @Autowired
    TradingConfig tradingConfig;

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Subscribe
    @AllowConcurrentEvents
    public void notifyByEmail(EventPayLoad<T> payLoad) {

        Preconditions.checkNotNull(payLoad);
        EmailContentGenerator<T> emailContentGenerator = eventEmailContentGeneratorMap.get(payLoad.getEvent());
        if (emailContentGenerator != null) {
            EmailPayLoad emailPayLoad = emailContentGenerator.generate(payLoad);
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setSubject(emailPayLoad.getSubject());
            msg.setFrom("forex@xquisiteart.com");

            msg.setTo(tradingConfig.getMailTo());
            msg.setText(emailPayLoad.getBody());
            this.mailSender.send(msg);
        } else {
            LOG.warn("No email content generator found for event:" + payLoad.getEvent().name());
        }
    }
}
