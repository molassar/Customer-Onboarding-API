package com.example.onboarding.infrastructure.persistence;

import com.example.onboarding.domain.customer.Customer;
import com.example.onboarding.domain.customer.CustomerRepository;
import com.example.onboarding.infrastructure.ratelimiting.LegacyDatabaseRateLimiter;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RateLimitedCustomerRepository implements CustomerRepository {
  private final SpringDataCustomerRepository delegate;
  private final LegacyDatabaseRateLimiter rateLimiter;

  public RateLimitedCustomerRepository(
      SpringDataCustomerRepository delegate, LegacyDatabaseRateLimiter rateLimiter) {
    this.delegate = delegate;
    this.rateLimiter = rateLimiter;
  }

  @Override
  public Customer save(Customer customer) {
    return rateLimiter.execute(() -> delegate.save(customer));
  }

  @Override
  public Optional<Customer> findByUsername(String username) {
    return rateLimiter.execute(() -> delegate.findByUsername(username));
  }

  @Override
  public Optional<Customer> findById(Long id) {
    return rateLimiter.execute(() -> delegate.findById(id));
  }

  @Override
  public Iterable<Customer> findAll() {
    return rateLimiter.execute(delegate::findAll);
  }

  @Override
  public long count() {
    return rateLimiter.execute(delegate::count);
  }
}
