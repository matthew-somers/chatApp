package com.uacf.chat.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This subclass was specifically created so it can reuse the primary key id
 * generated when ChatMessageHot was inserted into the db. A ChatMessageCold is
 * created when a ChatMessageHot has expired somehow.
 * 
 * @author Matthew Somers
 *
 */
@Entity
public class ChatMessageCold extends ChatMessage {

  private static final long serialVersionUID = 1L;

  @Id
  private long id;

  public ChatMessageCold() {
  };

  public ChatMessageCold(ChatMessageHot chatMessageHot) {
    super(chatMessageHot);
    this.id = chatMessageHot.getId();
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