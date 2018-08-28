package com.tudoreloprisan.email;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import com.tudoreloprisan.tradingAPI.account.TradingConfig;
import com.tudoreloprisan.tradingAPI.events.Event;
import com.tudoreloprisan.tradingAPI.events.EventPayLoad;

public class EventEmailNotifier<T> {

	private static final Logger				LOG	= Logger.getLogger(EventEmailNotifier.class);

	@Autowired
	JavaMailSender							mailSender;
	@Resource
	Map<Event, EmailContentGenerator<T>>	eventEmailContentGeneratorMap;
	@Autowired
	TradingConfig							tradingConfig;

	@Subscribe
	@AllowConcurrentEvents
	public void notifyByEmail(EventPayLoad<T> payLoad) {
		
		Preconditions.checkNotNull(payLoad);
		EmailContentGenerator<T> emailContentGenerator = eventEmailContentGeneratorMap.get(payLoad.getEvent());
		if (emailContentGenerator != null) {
			EmailPayLoad emailPayLoad = emailContentGenerator.generate(payLoad);
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setSubject(emailPayLoad.getSubject());
			
			msg.setTo(tradingConfig.getMailTo());
			msg.setText(emailPayLoad.getBody());			
			this.mailSender.send(msg);
		} else {
			LOG.warn("No com.tudoreloprisan.email content generator found for event:" + payLoad.getEvent().name());
		}
	}
}
