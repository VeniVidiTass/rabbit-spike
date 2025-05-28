package com.example.producer;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRecordRepository extends MongoRepository<MessageRecord, String> {}
