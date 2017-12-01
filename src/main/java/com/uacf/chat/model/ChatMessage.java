package com.uacf.chat.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This is the main ChatMessage object. It is not a specific jpa entity itself,
 * but the subclasses are and use these fields. It is populated with RequestBody
 * json from controller or when pulling from db. On initial creation of new
 * chat, expiration_date needs to be populated.
 * 
 * @author Matthew Somers
 *
 */
@MappedSuperclass
public abstract class ChatMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  private String username;
  private String text;
  private String expiration_date;

  // we don't need to save this, just use it for calculating expiration
  @JsonIgnore
  @Transient
  private int timeout;

  public ChatMessage() {
  };

  public ChatMessage(String username, String text, int timeout) {
    this.username = username;
    this.text = text;
    setExpirationWithTimeout(timeout);
  }

  public ChatMessage(ChatMessageHot chatMessage) {
    this.username = chatMessage.getUsername();
    this.text = chatMessage.getText();
    this.expiration_date = chatMessage.getExpiration_date();
  }

  public void setExpirationWithTimeout(int timeout) {
    Timestamp expirationTs = new Timestamp(System.currentTimeMillis() + (timeout * 1000));
    String expirationString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expirationTs);
    this.expiration_date = expirationString;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setExpiration_date(String expiration_date) {
    this.expiration_date = expiration_date;
  }

  public String getExpiration_date() {
    return expiration_date;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getTimeout() {
    return timeout;
  }

  public String toString() {
    return "username:" + getUsername() + ", text:" + getText() + ", expiration_date:" + getExpiration_date();
  }
}
