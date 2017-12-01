package com.uacf.chat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.uacf.chat.model.ChatMessage;
import com.uacf.chat.model.ChatMessageCold;
import com.uacf.chat.model.ChatMessageHot;
import com.uacf.chat.persistence.ChatMessageColdStorage;
import com.uacf.chat.persistence.ChatMessageHotStorage;

/**
 * These are the endpoints and much of the business logic for a simple chat app.
 * The main twist here is the concept of hot and cold storage that need to be
 * kept separate for expired and unexpired data. Currently, this is implemented
 * as just separate tables in the same db which is not how this would be in a
 * prd system. Still, it was fun to go through this.
 * 
 * @author Matthew Somers
 *
 */
@RestController
public class ChatController {

  @Autowired
  ChatMessageHotStorage hotStorage;

  @Autowired
  ChatMessageColdStorage coldStorage;

  /**
   * Returns a specific chat message for a given id passed through URL. First it
   * searches hot storage. If it is found in hot storage but is expired, it will
   * be moved to cold storage. If it is not found in hot storage, it searches cold
   * storage.
   * 
   * @param chatId
   *          The generated key used to identify the specific chat object
   * @return The specific chat object for a given chatId
   */
  @ResponseBody
  @RequestMapping(value = "/chat/{chatId}", method = RequestMethod.GET)
  public ChatMessage getChatById(@PathVariable long chatId) {
    ChatMessage chat;
    ChatMessageHot hotChat = hotStorage.findOne(chatId);
    ChatMessageCold coldChat;

    if (hotChat == null) { // not in hot storage ?
      System.out.println("chat id not in hot storage");
      coldChat = coldStorage.findOne(chatId);
      chat = coldChat;
      if (chat == null) { // not in cold storage either ?
        System.out.println("chat id not found");
      }
    } else if (isExpired(hotChat.getExpiration_date())) { // cleanup expired hotChats to cold storage
      System.out.println("Chat is expired");
      coldChat = new ChatMessageCold(hotChat);
      coldStorage.save(coldChat);
      hotStorage.delete(hotChat);
      chat = hotChat;
    } else { // found in hot storage and not expired
      chat = hotChat;
    }

    return chat;
  }

  /**
   * Returns all unexpired chats for a username passed through URL. Moves all of
   * these to cold storage
   * 
   * @param username
   *          The username to search hot storage against
   * @return An array of all unexpired chat messages for a username
   */
  @ResponseBody
  @RequestMapping(value = "/chats/{username}", method = RequestMethod.GET)
  public ArrayList<HashMap<Object, Object>> getChatsByUsername(@PathVariable String username) {
    Iterable<ChatMessageHot> hotChatList = hotStorage.findByusername(username);
    ArrayList<ChatMessageCold> coldChatList = new ArrayList<ChatMessageCold>();
    ArrayList<HashMap<Object, Object>> responseList = new ArrayList<HashMap<Object, Object>>();
    for (ChatMessageHot hotChat : hotChatList) {
      String expiration = hotChat.getExpiration_date();
      coldChatList.add(new ChatMessageCold(hotChat));

      if (!isExpired(expiration)) {
        HashMap<Object, Object> nonExpiredChatMap = new HashMap<Object, Object>();
        nonExpiredChatMap.put("id", hotChat.getId());
        nonExpiredChatMap.put("text", hotChat.getText());
        responseList.add(nonExpiredChatMap);

      } else {
        System.out.println("Chat: " + hotChat.getId() + " is expired");
      }
    }
    coldStorage.save(coldChatList);
    hotStorage.delete(hotChatList);

    return responseList;
  }

  /**
   * Sets new chat message into hot storage.
   * 
   * @param chat
   *          json string elements for username, text, and integer timeout
   * @return the generated id for this chat
   */
  @ResponseBody
  @RequestMapping(value = "/chat", method = RequestMethod.POST)
  public ResponseEntity chat(@RequestBody ChatMessageHot chat) {

    // validations and setting timeout properly
    if (chat.getUsername() == null || chat.getText() == null) {
      return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
    if (chat.getTimeout() == 0) {
      System.out.println("defaulting timeout");
      chat.setTimeout(60);
    }
    chat.setExpirationWithTimeout(chat.getTimeout());

    // persist it now and return the generated id
    hotStorage.save(chat);
    System.out.println("New chat: " + chat);
    HashMap<String, Long> responseId = new HashMap<String, Long>();
    responseId.put("id", chat.getId());

    return new ResponseEntity<>(responseId, HttpStatus.CREATED);
  }

  /**
   * Determines if a chat message is expired
   * 
   * @param chatExpiration
   *          String of the chat expiration
   * @return true if current system time is after chat expiration, false otherwise
   */
  private boolean isExpired(String chatExpiration) {
    try {
      Timestamp currentExpiration = new Timestamp(System.currentTimeMillis());
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      Date parsedDate = dateFormat.parse(chatExpiration);
      Timestamp chatExpirationTs = new Timestamp(parsedDate.getTime());
      System.out.println("Current ts: " + currentExpiration);
      System.out.println("Expiration ts: " + chatExpirationTs);
      return currentExpiration.after(chatExpirationTs);

    } catch (ParseException e) {
      e.printStackTrace();
      return true;
    }
  }

  /**
   * This is an example endpoint showing what could be done to keep the hot
   * storage performance high. This would not be good to actually have as a prd
   * endpoint. My initial thought to clean the hot storage was some automated job
   * scheduled as often as needed which basically does as below, moving expired
   * chatMessages to cold storage.
   */
  @RequestMapping(value = "/cleanHotStorage", method = RequestMethod.GET)
  public void cleanHotStorage() {
    Iterable<ChatMessageHot> hotChatList = hotStorage.findAll();
    ArrayList<ChatMessageHot> hotChatListToDelete = new ArrayList<ChatMessageHot>();
    ArrayList<ChatMessageCold> coldChatList = new ArrayList<ChatMessageCold>();
    for (ChatMessageHot hotChat : hotChatList) {
      String expiration = hotChat.getExpiration_date();

      if (isExpired(expiration)) {
        coldChatList.add(new ChatMessageCold(hotChat));
        hotChatListToDelete.add(hotChat);
      }
    }
    coldStorage.save(coldChatList);
    hotStorage.delete(hotChatList);
  }

  /**
   * Handles some parsing exceptions for endpoint input parameters
   */
  @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Input request not proper format")
  @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
  public void handleException() {

  }
}
