package com.example.producer;

import com.example.shared.Email;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailRecordRepository extends MongoRepository<Email, String> {}
