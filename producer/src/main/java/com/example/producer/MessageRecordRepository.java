package com.example.producer;

import com.example.shared.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRecordRepository extends MongoRepository<Message, String> {}
