package com.uacf.chat.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.uacf.chat.model.ChatMessageHot;

/**
 * This is the initial storage repo for new ChatMessages. After expiration date
 * has passed or some other condition, they are moved to cold storage. It is
 * pretty cool how this can generate sql based on the naming of a function I
 * don't have to implement
 * 
 * @author Matthew Somers
 *
 */
@RepositoryRestResource
public interface ChatMessageHotStorage extends CrudRepository<ChatMessageHot, Long> {
  List<ChatMessageHot> findByusername(String username);
}
