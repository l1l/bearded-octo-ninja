package com.ge.dsp.event.subscriber.core.impl.listener;

import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import com.ge.dsp.dsi.dups.api.IDups;
import com.ge.dsp.event.publisher.beans.MessageEvent;
import com.ge.dsp.event.subscriber.api.IEventCallback;
import com.ge.dsp.event.subscriber.beans.EventDestination;
import com.ge.dsp.event.subscriber.beans.EventDestination.NotificationType;
import com.ge.dsp.event.subscriber.core.entities.UserPreferenceEntity;
import com.ge.dsp.event.subscriber.core.fakes.IPreference;
import com.ge.dsp.event.subscriber.core.impl.SubscriberHelper;
import com.ge.dsp.event.subscriber.core.impl.SubscriberListener;
import com.ge.dsp.event.subscriber.core.impl.kernel.InternalConfiguration;
import com.ge.dsp.notification.api.INotificationBean;
import com.ge.dsp.notification.api.INotificationService;
import com.ge.dsp.notification.exception.NotificationException;

public class SubscriberListenerTestBase  {
	protected static final String TEST_CONTEXT_FOR_FILTERING = "testSendMessage_FilteredBy_Context";
	
	protected static final String CORRECT_JSON = "{\"eventFilter\":{\"eventType\":\"ALERT\",\"eventName\":null,\"context\":\"" +
			TEST_CONTEXT_FOR_FILTERING +
			"\"},\"notificationDestination\":{\"type\":\"EMAIL\",\"value\":[\"dsp.encore@ge.com\"]},\"notificationEnable\":true,\"digestPreference\":{\"digestFrequency\":\"daily\",\"digestTime\":18,\"dayOfMonth\":0,\"dayOfWeek\":null,\"digestEnable\":true},\"userIdentityID\":\"http://User.A@test.dsp.ge.com\"}";
	
	protected static final String JSON_WITH_NO_SUBSCRIPTION_SPECIFIED = "{\"eventFilter\":{\"eventType\":\"ALERT\",\"eventName\":null,\"context\":\"" +
			TEST_CONTEXT_FOR_FILTERING +
			"\"}";
	protected static final String NULL_JSON = null;
	
	protected InternalConfiguration internalConfig;
	protected IDups dupsServiceFake;
	protected MessageEvent fakeMessageEvent;
	protected FakeEventCallback fakeEventCallBack;
	protected INotificationService notification;
	protected SubscriberListener listener;
	protected SubscriberHelper subscriberHelper;
	protected IPreference preference;
	protected  List<UserPreferenceEntity> returnedPreferenceEntities;
	
	protected org.slf4j.Logger spyLogger;
	
	protected List<IPreference> preferenceListWith(IPreference preference) {
		List<IPreference> preferenceList = new ArrayList<IPreference>();
		preferenceList.add(preference);
		return preferenceList;
	}
	
	protected IPreference createPreferenceWith(String json) {
		preference = createEmptyUserPreference();
		preference.setDupsContext(TEST_CONTEXT_FOR_FILTERING);
		preference.setKey("A747/800/ALERT");
		preference.setUuid("http://User.A@test.dsp.ge.com");
		preference.setValue(json);
		return preference;
	}

	protected INotificationBean getActualBean() {
		return (INotificationBean) ((FakeNotification) notification).instantiatedBean;
	}

	protected INotificationService setNotificationService() {
		notification = new FakeNotification();
		internalConfig.setNotificationService(notification);
		return notification;
	}

	protected IPreference createEmptyUserPreference() {
		IPreference preference = new IPreference() {
			private String uuid;
			private String value;
			private String key;
			private String dupsContext;

			
			public String getUuid() {

				return this.uuid;
			}

			
			public void setUuid(String uuid) {
				this.uuid = uuid;
			}

			
			public String getValue() {

				return this.value;
			}

			
			public void setValue(String value) {
				this.value = value;
			}

			
			public String getKey() {
				return this.key;
			}

			
			public void setKey(String key) {
				this.key = key;
			}

			
			public String getDupsContext() {
				return this.dupsContext;
			}

			
			public void setDupsContext(String dupsContext) {
				this.dupsContext = dupsContext;
			}

		};
		return preference;
	}

	protected List<UserPreferenceEntity> aUserPreferenceEntityListWith(NotificationType notificationType) {
		List<UserPreferenceEntity> userPreferences = new ArrayList<UserPreferenceEntity>();
		UserPreferenceEntity userPreferenceEntity = new UserPreferenceEntity();
		userPreferenceEntity.setDigestEnabled(true);
		userPreferenceEntity.setDigestHour(8);
		userPreferenceEntity.setDupsContext("IVHM");

		EventDestination destination = new EventDestination();
		destination.setType(notificationType);
		
		List<String> emails = new ArrayList<String>();
		emails.add("dsp.encore@ge.com");

		destination.setValue(emails);
		userPreferenceEntity.setDestination(destination);
		userPreferenceEntity.setFrequence("hourly");
		userPreferenceEntity.setNotificationEnabled(Boolean.TRUE);
		userPreferenceEntity.setPreferenceIndex("fleet/500/ALERT");
		userPreferenceEntity.setUuid("http://User.A@test.dsp.ge.com");
		userPreferences.add(userPreferenceEntity);

		return userPreferences;

	}

	protected MessageEvent createGenericMessageEvent() {
		fakeMessageEvent = new MessageEvent();
		fakeMessageEvent.setContext(TEST_CONTEXT_FOR_FILTERING);
		fakeMessageEvent.setEventId("123");
		fakeMessageEvent.setEventTime("2012-08-14 09:00:02");
		fakeMessageEvent.setEventType("ALERT");
		fakeMessageEvent.setMessageBody("Notification Subscriber service unit test.");
		fakeMessageEvent.setMessageType("Notification");
		fakeMessageEvent.setMessageAttachment("abc".getBytes());
		fakeMessageEvent.setAttachmentContentType("application/pdf");
		fakeMessageEvent.setAttachmentName("report");
		fakeMessageEvent.setMessageSender("systemAdmin");
		fakeMessageEvent.setSystemId("100");
		fakeMessageEvent.setSystemType("fleet");

		return fakeMessageEvent;
	}

	protected void createNewListenerWith(IEventCallback fakeEventCallBack) {
		listener = new SubscriberListener(fakeEventCallBack);
		listener.setEventCallback(fakeEventCallBack);
		assertNotNull(listener.getEventCallback());
	}
	
	protected String createPreferenceIndex() {
		String preferenceIndex = "";
		if (fakeMessageEvent.getEventName() == null) preferenceIndex = preferenceIndex + "*";
		else preferenceIndex = preferenceIndex + fakeMessageEvent.getEventName();
	
		if (fakeMessageEvent.getEventType() == null) preferenceIndex = preferenceIndex + ".*";
		else preferenceIndex = preferenceIndex + "." + fakeMessageEvent.getEventType();
		
		preferenceIndex = preferenceIndex + "." + fakeMessageEvent.getContext();
		return preferenceIndex;
	}

	protected class FakeNotification implements INotificationService {
		public boolean wasCalled = false;
		public INotificationBean instantiatedBean;

		
		public void notify(INotificationBean actualNotificationBean)
				throws NotificationException {
			this.wasCalled = true;
			this.instantiatedBean = actualNotificationBean;
		}

	}
	
	protected class FakeEventCallback implements IEventCallback {
		public boolean processEventWasCalled = false;

		
		public void processEvent(MessageEvent event) {
			this.processEventWasCalled = true;

		}
	}

}
