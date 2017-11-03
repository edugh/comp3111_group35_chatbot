/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.BiConsumer;

import com.example.bot.spring.model.*;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.profile.UserProfileResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
public class KitchenSinkController {
	


	@Autowired
	private LineMessagingClient lineMessagingClient;

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		log.info("This is your entry point:");
		log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}

	@EventMapping
	public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		handleSticker(event.getReplyToken(), event.getMessage());
	}

	@EventMapping
	public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
		LocationMessageContent locationMessage = event.getMessage();
		reply(event.getReplyToken(), new LocationMessage(locationMessage.getTitle(), locationMessage.getAddress(),
				locationMessage.getLatitude(), locationMessage.getLongitude()));
	}

	@EventMapping
	public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
		final MessageContentResponse response;
		String replyToken = event.getReplyToken();
		String messageId = event.getMessage().getId();
		try {
			response = lineMessagingClient.getMessageContent(messageId).get();
		} catch (InterruptedException | ExecutionException e) {
			reply(replyToken, new TextMessage("Cannot get image: " + e.getMessage()));
			throw new RuntimeException(e);
		}
		DownloadedContent jpg = saveContent("jpg", response);
		reply(((MessageEvent) event).getReplyToken(), new ImageMessage(jpg.getUri(), jpg.getUri()));

	}

	@EventMapping
	public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
		final MessageContentResponse response;
		String replyToken = event.getReplyToken();
		String messageId = event.getMessage().getId();
		try {
			response = lineMessagingClient.getMessageContent(messageId).get();
		} catch (InterruptedException | ExecutionException e) {
			reply(replyToken, new TextMessage("Cannot get audio: " + e.getMessage()));
			throw new RuntimeException(e);
		}
		DownloadedContent mp4 = saveContent("mp4", response);
		reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100));
	}

	@EventMapping
	public void handleUnfollowEvent(UnfollowEvent event) {
		log.info("unfollowed this bot: {}", event);
	}

	@EventMapping
	public void handleFollowEvent(FollowEvent event) {
		String replyToken = event.getReplyToken();
		String cid = event.getSource().getUserId();
		if(database.getCustomer(cid)==null){
		    database.insertCustomer((cid));
        }
		List<Message> msgList = new ArrayList<>();
		msgList.add(new TextMessage("Welcome. This is travel chatbot No.35. What can I do for you?"));
        //msgList.add(new ImageMessage(url1, url2));
        msgList.add(new TextMessage("We don't have promotion image..."));
        reply(replyToken, msgList);
	}

	@EventMapping
	public void handleJoinEvent(JoinEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Joined " + event.getSource());
	}

	@EventMapping
	public void handlePostbackEvent(PostbackEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Got postback " + event.getPostbackContent().getData());
	}

	@EventMapping
	public void handleBeaconEvent(BeaconEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Got beacon message " + event.getBeacon().getHwid());
	}

	@EventMapping
	public void handleOtherEvent(Event event) {
		log.info("Received message(Ignored): {}", event);
	}

	private void reply(@NonNull String replyToken, @NonNull Message message) {
		reply(replyToken, Collections.singletonList(message));
	}

	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			BotApiResponse apiResponse = lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
			log.info("Sent messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "..";
		}
		this.reply(replyToken, new TextMessage(message));
	}


	private void handleSticker(String replyToken, StickerMessageContent content) {
		reply(replyToken, new StickerMessage(content.getPackageId(), content.getStickerId()));
	}

	/*
	For now, fall through handle methods until we match then handle directly.
	This is temporary so we can start working without too many conflicts
	TODO(Jason/all): When we decide how to handle forking logic replace this, probably with a match object
	 */
	private List<Message> tryHandleFAQ(String text, Source source) {
		// TODO(Jason): fuzzier match
		ArrayList<FAQ> faqs = database.getFAQs();
		for (FAQ faq: faqs) {
			if (text.equals(faq.question)) {
				return Collections.singletonList(new TextMessage(faq.answer));
			}
		}
		return null;
	}

	private List<Message> tryHandleAmountOwed(String text, Source source) {
		return null;
	}

	private List<Message> tryHandleBookingRequest(String text, Source source) {
	    //TODO(Shuo): workflow
		String cid = source.getUserId();
        Customer customer = database.getCustomer(cid);
        Booking booking = database.getCurrentBooking((cid));
        List<Message> msgList= new ArrayList<>();
        String state = customer.state;
        String pid = null;
        java.sql.Date date = null;
        Tour tour = null;
        if(booking != null){
            //At least booking are with cid, pid
            pid = booking.planId;
            if(booking.tourDate != null){
                date = booking.tourDate;
                tour = database.getTour(pid, date);
            }
            date = booking.tourDate;
        }

        switch (state){
            case "new":
            	//go into tourSearch
				return null;
            //enter from tourSearch
            case "reqPlanId":
                database.insertBooking(cid, pid);
                if(customer.name == null){
                    database.updateCustomerState(cid, "reqName");
                    msgList.add(new TextMessage("What's your name, please?"));
                }
                else{
                    database.updateCustomerState(cid, "reqDate");
                    msgList.add(new TextMessage("When are you planing to set out? Please answer in YYYYMMDD."));
                }
            case "reqName":
                database.updateCustomer(cid, "name", filterString(text));
                database.updateCustomerState(cid, "reqGender");
                msgList.add(new TextMessage("Male or Female please?"));
            case "reqGender":
                database.updateCustomer(cid, "gender", filterString(text));
                database.updateCustomerState(cid, "reqAge");
                msgList.add(new TextMessage("How old are you please?"));
            case "reqAge":
                database.updateCustomer(cid, "age", getIntFromText(text));
                database.updateCustomerState(cid, "reqPhoneNumber");
                msgList.add(new TextMessage("Phone number please?"));
            case "reqPhoneNumber":
                database.updateCustomer(cid, "gender", filterString(text));
                database.updateCustomerState(cid, "reqDate");
                msgList.add(new TextMessage("When are you planing to set out? Please answer in YYYYMMDD.")); //TODO
            case "reqDate":
                tour = database.getTour(pid, getDateFromText(text));
                if(database.isTourFull()){
                    msgList.add(new TextMessage("Sorry it is full-booked that day. What about other trips or departure date?"));
                    // database.updateCustomerState(cid, "changeDateOrPlan");
                }
                else{
                    database.updateBookingDate(cid, pid, getDateFromText(text));
                    msgList.add(new TextMessage("How many adults(Age>11) are planning to go?"));
                    database.updateCustomerState(cid, "reqNAdult");
                }
            case "reqNAdult":
                database.updateBooking(cid, pid, date, "adults", getIntFromText(text));
                msgList.add(new TextMessage("How many children (Age 4 to 11) are planning to go?"));
                database.updateCustomerState(cid, "reqNChild");
            case "reqNChild":
                database.updateBooking(cid, pid, date, "children", getIntFromText(text));
                msgList.add(new TextMessage("How many children (Age 0 to 3) are planning to go?"));
                database.updateCustomerState(cid, "reqNToddler");
            case "reqNToddler":
                database.updateBooking(cid, pid, date, "toddlers", getIntFromText(text));
                msgList.add(new TextMessage("Confirmed?")); //TODO Optionally: show fee
                database.updateCustomerState(cid, "reqConfirm");
            case "reqConfirm":
                if(isYes(text)) {
                    database.updateCustomerState(cid, "booked");
                    //TODO: calculate and add msg fee
                    msgList.add(new TextMessage("Thank you. Please pay the tour fee by ATM to 123-345-432-211 of ABC Bank or by cash in our store. When you complete the ATM payment, please send the bank in slip to us. Our staff will validate it."));
                }
                else {
                    msgList.add(new TextMessage("Why? Fuck you. Say YES."));
                }

            default:
                return null;
        }

	}

	public boolean isYes(String answer){
	    if(answer.toLowerCase().contains(new String("yes").toLowerCase())){
	        return true;
        }
        //TODO: yes, yep, yeah, ok, of course, sure, why not
        return false;
    }

    public java.sql.Date getDateFromText(String answer)throws ParseException {
	    //TODO: standardize YYYYMMDD
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        java.util.Date parsed = format.parse(answer);
        return(new java.sql.Date(parsed.getTime()));
    }

    public int getIntFromText(String answer){
        //TODO: filterString
        return Integer.parseInt(answer);
    }

    public String filterString(String answer){
        //TODO: I'm XX -> XX, Male -> M
        return answer;
    }



	private List<Message> tryHandleTourSearch(String text, Source source) {
		// TODO(Jason): match less idiotically, parse parameters
		if (text.equals("Which tours are available")) {
			// TODO(Jason): real search
			ArrayList<Plan> plans = database.getPlans();
			if (plans.size() == 0) {
				return Collections.singletonList(new TextMessage("No tours found"));
			} else {
				ArrayList<Message> messages = new ArrayList<>();
				for (Plan plan : plans) {
					messages.add(new TextMessage(String.format("%s:\n%s\n\n", plan.name, plan.shortDescription)));
				}
				database.updateCustomerState(source.getUserId(),"reqPlanId");
				return messages;
			}
		}
		return null;
	}

	private List<Message> handleUnknownQuery(String text, Source source) {
		return Collections.singletonList(new TextMessage("I don't understand your question, try rephrasing"));
		// TODO: Store question for report
	}

	private void handleTextContent(String replyToken, Event event, TextMessageContent content)
            throws Exception {
        String text = content.getText();
		Source source = event.getSource();
        log.info("Got text message from {}: {}", replyToken, text);

		@SuppressWarnings("unchecked")
		BiFunction<String, Source, List<Message>>[] handleFunctions =
					(BiFunction<String, Source, List<Message>>[]) new BiFunction[] {
				(BiFunction<String, Source, List<Message>>) this::tryHandleFAQ,
				(BiFunction<String, Source, List<Message>>) this::tryHandleAmountOwed,
				(BiFunction<String, Source, List<Message>>) this::tryHandleBookingRequest,
				(BiFunction<String, Source, List<Message>>) this::tryHandleTourSearch,
				(BiFunction<String, Source, List<Message>>) this::handleUnknownQuery,
		};
		for (BiFunction<String, Source, List<Message>> handleFunction: handleFunctions) {
			List<Message> response = handleFunction.apply(text, source);
			if (response != null) {
				this.reply(replyToken, response);
				return;
			}
		}
    }

	static String createUri(String path) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();
	}

	private void system(String... args) {
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		try {
			Process start = processBuilder.start();
			int i = start.waitFor();
			log.info("result: {} =>  {}", Arrays.toString(args), i);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (InterruptedException e) {
			log.info("Interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
		log.info("Got content-type: {}", responseBody);

		DownloadedContent tempFile = createTempFile(ext);
		try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
			ByteStreams.copy(responseBody.getStream(), outputStream);
			log.info("Saved {}: {}", ext, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static DownloadedContent createTempFile(String ext) {
		String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
		Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
		tempFile.toFile().deleteOnExit();
		return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));
	}


	


	public KitchenSinkController() {
		database = new SQLDatabaseEngine();
		itscLOGIN = System.getenv("ITSC_LOGIN");
	}

	private DatabaseEngine database;
	private String itscLOGIN;
	

	//The annontation @Value is from the package lombok.Value
	//Basically what it does is to generate constructor and getter for the class below
	//See https://projectlombok.org/features/Value
	@Value
	public static class DownloadedContent {
		Path path;
		String uri;
	}
}
