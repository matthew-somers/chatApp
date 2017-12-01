package com.uacf.chat.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This subclass was specifically created so it can generate the primary key id
 * field below upon insertion to a db. Upon passing the expiration date, an
 * object of this type should be moved to a ChatMessageCold and out of hot
 * storage.
 * 
 * @author Matthew Somers
 *
 */
@Entity
public class ChatMessageHot extends ChatMessage {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  public ChatMessageHot() {
  };

  public ChatMessageHot(String username, String text, int timeout) {
    super(username, text, timeout);
  }

  @JsonIgnore
  public long getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String toString() {
    return "id: " + id + ", " + super.toString();
  }
}
