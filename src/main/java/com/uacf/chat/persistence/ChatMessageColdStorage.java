package com.uacf.chat.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.uacf.chat.model.ChatMessageCold;

/**
 * This is the 'long term' storage. Messages are moved from hot storage to here.
 * It is pretty cool how this can generate sql based on the naming of a function
 * I don't have to implement
 * 
 * @author Matthew Somers
 *
 */
@RepositoryRestResource
public interface ChatMessageColdStorage extends CrudRepository<ChatMessageCold, Long> {

  List<ChatMessageCold> findByusername(String username);
}
