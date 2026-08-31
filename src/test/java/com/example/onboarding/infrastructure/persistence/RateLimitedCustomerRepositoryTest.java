package com.example.onboarding.infrastructure.persistence;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.onboarding.domain.customer.Customer;
import com.example.onboarding.infrastructure.ratelimiting.LegacyDatabaseRateLimiter;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class RateLimitedCustomerRepositoryTest {

  @Test
  void shouldRateLimitBeforeCallingRepository() {
    var delegate = mock(SpringDataCustomerRepository.class);
    var rateLimiter = mock(LegacyDatabaseRateLimiter.class);
    var repository = new RateLimitedCustomerRepository(delegate, rateLimiter);
    var customer = mock(Customer.class);

    when(rateLimiter.execute(org.mockito.ArgumentMatchers.any()))
        .thenAnswer(invocation -> invocation.<Supplier<?>>getArgument(0).get());
    when(delegate.save(customer)).thenReturn(customer);

    repository.save(customer);

    var order = inOrder(rateLimiter, delegate);
    order.verify(rateLimiter).execute(org.mockito.ArgumentMatchers.<Supplier<Customer>>any());
    order.verify(delegate).save(customer);
  }
}
