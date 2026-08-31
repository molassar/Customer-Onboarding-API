package com.example.onboarding.domain.account;

import java.util.Optional;

/** Port for reading an account overview without loading the complete customer aggregate. */
public interface AccountOverviewRepository {
  /** Finds the account overview belonging to the supplied customer. */
  Optional<AccountOverview> findByCustomerId(Long customerId);
}
