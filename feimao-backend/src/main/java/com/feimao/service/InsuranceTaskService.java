package com.feimao.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class InsuranceTaskService {
  private final InsuranceService insurance;

  public InsuranceTaskService(InsuranceService insurance) {
    this.insurance = insurance;
  }

  @Scheduled(fixedDelayString = "${business.insurance.task-delay-ms:600000}")
  public void synchronizePolicies() {
    insurance.insurePendingGuests();
    insurance.cancelPendingGuests();
  }

  @Async
  public void synchronizePoliciesNow() {
    synchronizePolicies();
  }
}
