package com.dawnsynch.darajaapitutorial.repository;

import com.dawnsynch.darajaapitutorial.entity.STKCallbackLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface STKPushLogRepository extends JpaRepository<STKCallbackLog, Long> {
}
