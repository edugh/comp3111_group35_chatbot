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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;

import ai.api.model.Result;
import com.example.bot.spring.model.*;
import com.example.bot.spring.model.Booking;
import com.example.bot.spring.model.Plan;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.event.source.Source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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

    private AIApiWrapper aiApiWrapper;
    
    private static final String PREVIEW_IMG_URL = "https://i.imgur.com/kQNwgcK.jpg";
	private static final String FULL_IMG_URL = "https://i.imgur.com/RpIsqnC.jpg";

    private static final String AMOUNT_OWED = "AmountOwed";
    private static final String BOOK_TOUR = "BookTour";
    private static final String ENROLLED_TOURS = "EnrolledTours";
    private static final String TOUR_SEARCH = "TourSearch";
    private static final String FAQ_PREFIX = "FAQ";

    private static final String GIVE_AGE = "GiveAge";
    private static final String GIVE_GENDER = "GiveGender";
    private static final String GIVE_NAME = "GiveName";
    private static final String GIVE_NUMBER = "GiveNumber";

    private static final String GIVE_DEPARTURE_DATE = "GiveDepartureDate";
    private static final String GIVE_ADULTS = "GiveAdults";
    private static final String GIVE_CHILDREN = "GiveChildren";
    private static final String GIVE_TODDLERS = "GiveToddlers";
    private static final String GIVE_CONFIRMATION = "GiveConfirmation";
    private static final String CANCEL_CONFIRMATION = "CancelConfirmation";

    private static final String DISCOUNT = "Discount";

    private static final String CHANNEL_TOKEN = "86G0LghgbbwHzoX8UIIvnaMGMAAJL6/mXQQEWNat4Jlsk0dRMaC91ksPZtG1whpuma/7LJsBO/UVqY7eweieJGNdOHnimA5dW4ElA3QBeVOlBGmqk+c+ypmGrdzuir8nLfpMD4Yc/7Vciz8wbbizTgdB04t89/1O/w1cDnyilFU=";

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
        String customerId = event.getSource().getUserId();
        if (!database.getCustomer(customerId).isPresent()) {
            database.insertCustomer((customerId));
        }
        List<Message> msgList = new ArrayList<>();
		msgList.add(new TextMessage("Welcome. This is travel chatbot No.35."));
		msgList.add(new ImageMessage(FULL_IMG_URL, PREVIEW_IMG_URL));
		msgList.add(new TextMessage("What can I do for you?"));
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

    protected void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            log.info("Sending messages:");
            for (Message message : messages) {
                if (message instanceof TextMessage) {
                    log.info("\t{}", ((TextMessage) message).getText());
                }
            }
            BotApiResponse apiResponse = lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void push(@NonNull String userId, @NonNull List<Message> messages) {
        PushMessage pushMessage = new PushMessage(userId, messages);
        try {
            LineMessagingServiceBuilder.create(CHANNEL_TOKEN).build().pushMessage(pushMessage).execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void push(@NonNull Set<String> userId, @NonNull Message message) {
        Multicast pushMessage = new Multicast(userId, message);
        try {
            LineMessagingServiceBuilder.create(CHANNEL_TOKEN).build().multicast(pushMessage).execute();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void pushDiscount(String planId, Date date) {
        Plan plan = database.getPlan(planId).orElseThrow(() -> new IndexOutOfBoundsException("Can't push discount because plan doesn't exist"));
        String nlpDate = new SimpleDateFormat("yyyy/MM/dd").format(date);
        String message = String.format("Double 11 Festival discount! First 4 reply will get a 50% discount " +
                        "in %s on %s. Please reply 'Discount n seats for %s on %s'. The n here is the number of seats you book, 1 or 2.",
                plan.name, nlpDate, planId, nlpDate);
        push(database.getCustomerIdSet(), new TextMessage(message));
    }

    public String handleDiscount(Result aiResult, Source source) {
        String customerId = source.getUserId();
        String numberTickets = aiResult.getStringParameter("number-integer");
        String planId = aiResult.getStringParameter("any");
        Date tourDate = new Date(aiResult.getDateParameter("date").getTime());

        boolean discountWorked = database.insertDiscount(customerId, planId, tourDate);
        return discountWorked? "Discount successfully" : "Sorry discount sold out";
    }

    @Scheduled(cron = "0 0 * * * ?")
    private void schedulePushDiscount() {
        Timestamp now = Timestamp.from(
                Timestamp.valueOf(LocalDateTime.now()).toInstant().truncatedTo(ChronoUnit.HOURS));
        List<DiscountSchedule> listDiscountSchedule = database.getDiscountSchedules(now);
        for (DiscountSchedule ds : listDiscountSchedule) {
            pushDiscount(ds.planId, ds.tourDate);
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

    private String handleFAQ(Result aiResult) {
        return database.getFAQ(aiResult.getMetadata().getIntentName())
                .map(faq -> faq.answer)
                .orElse("I was told to handle this as an FAQ, but it is not one.");
    }

    private String handleAmountOwed(Source source) {
        BigDecimal amountOwed = database.getAmountOwed(source.getUserId());
        String prettyAmount = NumberFormat.getCurrencyInstance().format(amountOwed);
        return String.format("You owe %s", prettyAmount);
    }

    private List<Message> handleEnrolledTours(Source source) {
        ArrayList<Booking> bookings = database.getBookings(source.getUserId());
        if (bookings.size() == 0) {
            return Collections.singletonList(new TextMessage("You currently have no bookings"));
        } else {
            ArrayList<Message> messages = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            for (Booking booking : bookings) {
                messages.add(new TextMessage(String.format("%s:\n%s", booking.planId, simpleDateFormat.format(booking.tourDate))));
            }
            return messages;
        }
    }

    private String handleGiveName(Result aiResult, Source source) {
        String customerId = source.getUserId();
        String givenName = aiResult.getStringParameter("given-name");
        String lastName = aiResult.getStringParameter("last-name", null);
        database.updateCustomer(customerId, "name", lastName == null ? givenName : givenName + " " + lastName);
        return "Male or Female please?";
    }

    private String handleGiveGender(Result aiResult, Source source) {
        String customerId = source.getUserId();
        String gender = aiResult.getStringParameter("Gender");
        database.updateCustomer(customerId, "gender", Utils.getGender(gender));
        return "How old are you please?";
    }

    private String handleGiveAge(Result aiResult, Source source) {
        String customerId = source.getUserId();
        int age = aiResult.getIntParameter("number-integer");
        database.updateCustomer(customerId, "age", age);
        return "Phone number please?";
    }

    private String handleGiveNumber(Result aiResult, Source source) {
        String customerId = source.getUserId();
        String phoneNumber = aiResult.getStringParameter("phone-number");
        database.updateCustomer(customerId, "phoneNumber", Utils.filterString(phoneNumber));
        return "When are you planing to set out? Please answer in YYYY/MM/DD.";
    }

    private String handleBookingRequest(Result aiResult, Source source) {
        String customerId = source.getUserId();
        Customer customer = database.getCustomer(customerId).get(); //TODO: what if customer not in DB?
        String tour = aiResult.getStringParameter("Tour");
        ArrayList<Plan> plans = database.getPlans();
        Plan requestedPlan = plans.stream()
                .filter(possiblePlan -> possiblePlan.id.equals(tour) || Utils.stupidFuzzyMatch(tour, possiblePlan.name))
                .findFirst().orElse(null);
        if (requestedPlan == null) {
            return "Couldn't find that tour, try again.";
        } else {
            database.insertBooking(customerId, requestedPlan.id);
            if (customer.name == null) {
                return "What's your name, please?";
            } else {
                return "When are you planing to set out? Please answer in YYYY/MM/DD.";
            }
        }
    }

    private String handleGiveDeparture(Result aiResult, Source source) {
        String customerId = source.getUserId();
        Booking booking = database.getCurrentBooking(customerId);
        String planId = booking.planId;

        java.util.Date date = aiResult.getDateParameter("date-time");
        Date sqlDate = new java.sql.Date(date.getTime());
        if (database.isTourFull(planId, sqlDate)) {
            return "Sorry it is full-booked that day. What about other trips or departure date?";
        } else {
            database.updateBookingDate(customerId, planId, sqlDate);
            database.updateCustomerState(customerId, "reqNAdult");
            return "How many adults(Age>11) are planning to go?";
        }
    }

    private String handleGiveAdults(Result aiResult, Source source) {
        String customerId = source.getUserId();
        Booking booking = database.getCurrentBooking(customerId);
        String planId = booking.planId;
        Date date = booking.tourDate;

        int adults = aiResult.getIntParameter("number-integer");
        database.updateBooking(customerId, planId, date, "adults", adults);
        database.updateCustomerState(customerId, "reqNChild");
        return "How many children (Age 4 to 11) are planning to go?";
    }

    private String handleGiveChildren(Result aiResult, Source source) {
        String customerId = source.getUserId();
        Booking booking = database.getCurrentBooking(customerId);
        String planId = booking.planId;
        Date date = booking.tourDate;

        int children = aiResult.getIntParameter("number-integer");
        database.updateBooking(customerId, planId, date, "children", children);
        database.updateCustomerState(customerId, "reqNToddler");
        return "How many children (Age 0 to 3) are planning to go?";
    }

    private String handleGiveToddlers(Result aiResult, Source source) {
        String customerId = source.getUserId();
        Booking booking = database.getCurrentBooking(customerId);
        String planId = booking.planId;
        Date date = booking.tourDate;

        int toddlers = aiResult.getIntParameter("number-integer");
        database.updateBooking(customerId, planId, date, "toddlers", toddlers);
        database.updateCustomerState(customerId, "reqNToddler");
        return "Confirmed?"; //TODO Optionally: show fee
    }

    private String handleGiveConfirmation(Source source) {
        String customerId = source.getUserId();
        Booking booking = database.getCurrentBooking(customerId);
        String planId = booking.planId;
        Date date = booking.tourDate;

        database.updateCustomerState(customerId, "booked");
        Plan confirmedPlan = database.getPlan(booking.planId)
                .orElseThrow(() -> new IndexOutOfBoundsException("Can't give confirmation because plan to confirm doesn't exist"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(booking.tourDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
        BigDecimal pricePerPerson = isWeekend ? confirmedPlan.weekendPrice : confirmedPlan.weekdayPrice;
        int nDiscount = database.checkDiscount(customerId, planId, date);
        BigDecimal numPeople = new BigDecimal(booking.adults + (((float) booking.children) / 2));
        if (booking.adults < nDiscount) {
            numPeople = numPeople.subtract(new BigDecimal(
                    ((float) booking.adults) / 2 + ((float) (java.lang.Math.min(nDiscount, booking.children) - booking.adults)) / 4));
        } else {
            numPeople = numPeople.subtract(new BigDecimal(
                    ((float) nDiscount) / 2));
        }
        BigDecimal fee = pricePerPerson.multiply(numPeople);
        database.updateBooking(customerId, planId, date, "fee", fee);
        database.updateBooking(customerId, planId, date, "paid", BigDecimal.ZERO);
        return "Thank you. Please pay the tour fee by ATM to 123-345-432-211 of ABC Bank or by cash in our store. When you complete the ATM payment, please send the bank in slip to us. Our staff will validate it.";
    }

    private String handleCancelConfirmation(Result aiResult, Source source) {
        //TODO(Jason): do it
        return "Booking calcelled";
    }

    private List<Message> handleTourSearch(Result aiResult) {
        java.util.Date date = aiResult.getDateParameter("date");
        String keywords = aiResult.getStringParameter("any");
        // TODO(Jason) also deal with date ranges eg next weekend or next week

        Iterator<Plan> plans = Utils.filterAndSortTourResults(date, keywords, database.getPlans());
        if (!plans.hasNext()) {
            return Collections.singletonList(new TextMessage("No tours found"));
        } else {
            ArrayList<Message> messages = new ArrayList<>();
            plans.forEachRemaining(plan -> messages.add(new TextMessage(String.format("%s: %s - %s", plan.id, plan.name, plan.shortDescription))));
            messages.add(new TextMessage("Here are some tours that may interest you, please respond which one you would like to book"));
            return messages;
        }
    }

    private String handleUnknowDialogue(String receivedText, Source source) {
        String customerId = source.getUserId();
        Timestamp receiveDateTime = new Timestamp(System.currentTimeMillis());
        Dialogue newDialogue = new Dialogue(customerId, receiveDateTime, receivedText);
        database.insertDialogue(newDialogue);
        return "I don't understand your question, try rephrasing";
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws Exception {
        String text = content.getText();
        Source source = event.getSource();
        log.info("Got text message from {}: {}", replyToken, text);

        Result aiResult = AIApiWrapper.getIntent(text, source);
        String intentName = aiResult.getMetadata().getIntentName();
        log.info("Received intent from api.ai: {}", intentName);

        if (intentName == null) {
            this.replyText(replyToken, handleUnknowDialogue(text, source));
            return;
        }
        switch (intentName) {
            case AMOUNT_OWED:
                this.replyText(replyToken, handleAmountOwed(source));
                break;
            case BOOK_TOUR:
                this.replyText(replyToken, handleBookingRequest(aiResult, source));
                break;
            case ENROLLED_TOURS:
                this.reply(replyToken, handleEnrolledTours(source));
                break;
            case TOUR_SEARCH:
                this.reply(replyToken, handleTourSearch(aiResult));
                break;
            case GIVE_NAME:
                this.replyText(replyToken, handleGiveName(aiResult, source));
                break;
            case GIVE_GENDER:
                this.replyText(replyToken, handleGiveGender(aiResult, source));
                break;
            case GIVE_AGE:
                this.replyText(replyToken, handleGiveAge(aiResult, source));
                break;
            case GIVE_NUMBER:
                this.replyText(replyToken, handleGiveNumber(aiResult, source));
                break;

            case GIVE_DEPARTURE_DATE:
                this.replyText(replyToken, handleGiveDeparture(aiResult, source));
                break;
            case GIVE_ADULTS:
                this.replyText(replyToken, handleGiveAdults(aiResult, source));
                break;
            case GIVE_CHILDREN:
                this.replyText(replyToken, handleGiveChildren(aiResult, source));
                break;
            case GIVE_TODDLERS:
                this.replyText(replyToken, handleGiveToddlers(aiResult, source));
                break;
            case GIVE_CONFIRMATION:
                this.replyText(replyToken, handleGiveConfirmation(source));
                break;
            case CANCEL_CONFIRMATION:
                this.replyText(replyToken, handleCancelConfirmation(aiResult, source));
                break;
            case DISCOUNT:
                this.replyText(replyToken, handleDiscount(aiResult, source));
            default:
                if (intentName.startsWith(FAQ_PREFIX)) {
                    this.replyText(replyToken, handleFAQ(aiResult));
                } else {
                    this.replyText(replyToken, handleUnknowDialogue(text, source));
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
        this(DatabaseEngine.connectToProduction());
    }

    public KitchenSinkController(DatabaseEngine databaseEngine) {
        this.database = databaseEngine;
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
