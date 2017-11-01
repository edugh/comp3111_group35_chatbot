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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.example.bot.spring.model.FAQ;
import com.example.bot.spring.model.Tour;
import com.linecorp.bot.model.profile.UserProfileResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
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
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.sun.jmx.remote.util.OrderClassLoaders;
import com.sun.prism.Image;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

import javax.imageio.ImageIO;

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
		database.newCostomer(event.getSource().getUserId());
		String replyToken = event.getReplyToken();
		this.replyText(replyToken,
				"Welcome to COMP3111 Travel. This is Chatbot No.35. What can I do for you?"
				+ "\n" + database.promoteTour());
		//this.replyImage(replyToken,url1,url2);
		//List<com.sun.xml.internal.ws.wsdl.writer.document.Message> msgList= new List<Message>();
		//msgList.add(TextMessage("Welcome to COMP3111 Travel. This is Chatbot No.35. What can I do for you?"));
		//msgList.add(TextMessage(database.promoteTour()));
		//this.reply(replyToken, msgList);
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
	
	private void replyImage(@NonNull String replyToken, @NonNull String urlOriginal, @NonNull String urlPrview) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		URL urlOri = new URL(urlOriginal);
		URL urlPre = new URL(urlPrview)
		URLConnection connectionOri = urlOri.openConnection();
		URLConnection connectionPre = urlPre.openConnection();
		BufferedImage imgOri = ImageIO.read(connectionOri.getInputStream());
		BufferedImage imgPre = ImageIO.read(connectionPre.getInputStream());
		if (imgOri.getWidth()>1024 || imgOri.getHeight()>1024
				|| imgPre.getWidth()>240 || imgPre.getHeight()>240) { 
			throw new IllegalArgumentException("image too big");
		}
		this.reply(replyToken, new ImageMessage(urlOriginal, urlPrview));
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

	private List<Message> tryHandleTourSearch(String text, Source source) {
		if (text.equals("Which tours are available")) {
			ArrayList<Tour> tours = database.getTours();
			if (tours.size() == 0) {
				return Collections.singletonList(new TextMessage("No tours found"));
			} else {
				ArrayList<Message> messages = new ArrayList<>();
				for (Tour tour : tours) {
					messages.add(new TextMessage(String.format("%s:\n%s\n\n", tour.name, tour.shortDescription)));
				}
				return messages;
			}
		}
		return null;
	}

	private List<Message> tryHandleBookingRequest(String text, Source source) {
		return null;
	}

	private List<Message> handleUnknownQuery(String text, Source source) {
		return Collections.singletonList(new TextMessage("I don't understand your question, try rephrasing"));
	}

	private void handleTextContent(String replyToken, javafx.event.Event event, TextMessageContent content)
            throws Exception {
        String text = content.getText();
        String cid = event.getSource().getUserId();
        log.info("Got text message from {}: {}", replyToken, text);
        Ordering ordering = database.readOrdering(cid);
        if(ordering == null) {
        	//cust has ever ordered
        	//judge if book another trip -> newOrdering
        	//else -> answer questions

        else {
        	//-> check state
        	//-> judge if the answer is to the question
        	//-> update something
        	switch(ordering.state()) {
	        	case "new":{
	        		
	        		break;
	        	}
	        	default:{
	        		
	        		break;
	        	}
        	}
        }
        

		@SuppressWarnings("unchecked")
		BiFunction<String, Source, List<Message>>[] handleFunctions = new BiFunction[] {
				(BiFunction<String, Source, List<Message>>) this::tryHandleFAQ,
				(BiFunction<String, Source, List<Message>>) this::tryHandleAmountOwed,
				(BiFunction<String, Source, List<Message>>) this::tryHandleBookingRequest,
				(BiFunction<String, Source, List<Message>>) this::tryHandleTourSearch,
				(BiFunction<String, Source, List<Message>>) this::handleUnknownQuery
		};
		for (BiFunction<String, Source, List<Message>> handleFunction: handleFunctions) {
			List<Message> response = handleFunction.apply(text, event.getSource());
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
