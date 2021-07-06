package devops.tim9.userservice.producer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devops.tim9.userservice.domain.LoginEvent;
import devops.tim9.userservice.domain.UserEvent;

@Component
public class UserEventProducer {

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	ObjectMapper objectMapper;

	public void sendUserEvent(UserEvent userEvent) throws JsonProcessingException {
		Integer key = userEvent.getUserEventId();
		String value = objectMapper.writeValueAsString(userEvent);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
			@Override
			public void onFailure(Throwable ex) {

			}

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}
		});
	}

	private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
		System.out.println("Message Sent Successfully for the key: {} and the value is {} , partition is {}" +  key +  value + result.getRecordMetadata());

	}

	private void handleFailure(Integer key, String value, Throwable ex) {
		System.out.println("Message Sent Successfully for the key: {} and the value is {} , partition is {}" + ex.getMessage());

	}

	public void sendLoginEvent(LoginEvent loginEvent) throws JsonProcessingException {
		Integer key = loginEvent.getLoginEventId();
		String value = objectMapper.writeValueAsString(loginEvent);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send("login-events", key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
			@Override
			public void onFailure(Throwable ex) {

			}

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}
		});
	}
}
