package com.example.onboarding.infrastructure.persistence;

import com.example.onboarding.domain.account.AccountOverview;
import com.example.onboarding.domain.account.AccountOverviewRepository;
import com.example.onboarding.domain.account.AccountType;
import com.example.onboarding.domain.account.CurrencyCode;
import com.example.onboarding.domain.account.Iban;
import com.example.onboarding.infrastructure.ratelimiting.LegacyDatabaseRateLimiter;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAccountOverviewRepository implements AccountOverviewRepository {
  private final JdbcClient jdbcClient;
  private final LegacyDatabaseRateLimiter rateLimiter;

  public JdbcAccountOverviewRepository(
      JdbcClient jdbcClient, LegacyDatabaseRateLimiter rateLimiter) {
    this.jdbcClient = jdbcClient;
    this.rateLimiter = rateLimiter;
  }

  @Override
  public Optional<AccountOverview> findByCustomerId(Long customerId) {
    return rateLimiter.execute(
        () ->
            jdbcClient
                .sql(
                    """
                    SELECT iban, account_type, balance, currency
                    FROM account
                    WHERE customer_id = :customerId
                    """)
                .param("customerId", customerId)
                .query(
                    (resultSet, rowNumber) ->
                        new AccountOverview(
                            new Iban(resultSet.getString("iban")),
                            AccountType.valueOf(resultSet.getString("account_type")),
                            resultSet.getBigDecimal("balance"),
                            CurrencyCode.valueOf(resultSet.getString("currency"))))
                .optional());
  }
}
