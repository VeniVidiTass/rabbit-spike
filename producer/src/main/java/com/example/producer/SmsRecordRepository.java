package com.example.producer;

import com.example.shared.Sms;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsRecordRepository extends MongoRepository<Sms, String> {}
