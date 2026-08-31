package com.example.onboarding.domain.account;

import com.example.onboarding.domain.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;

/** Retrieves account information for an authenticated customer. */
@Service
public class AccountOverviewService {
  private final AccountOverviewRepository accountOverviewRepository;

  public AccountOverviewService(AccountOverviewRepository accountOverviewRepository) {
    this.accountOverviewRepository = accountOverviewRepository;
  }

  /**
   * Finds the account overview owned by the given customer.
   *
   * @throws AccountNotFoundException when the customer has no account
   */
  public AccountOverview getOverview(Long customerId) {
    return accountOverviewRepository
        .findByCustomerId(customerId)
        .orElseThrow(() -> new AccountNotFoundException(customerId));
  }
}
