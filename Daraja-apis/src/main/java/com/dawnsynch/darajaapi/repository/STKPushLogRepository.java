package com.dawnsynch.darajaapi.repository;

import com.dawnsynch.darajaapi.entity.STKCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface STKPushLogRepository extends JpaRepository<STKCallbackLog, Long> {
}
