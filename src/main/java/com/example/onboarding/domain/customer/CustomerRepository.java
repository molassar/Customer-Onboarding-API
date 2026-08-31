package com.example.onboarding.domain.customer;

import java.util.Optional;

/** Persistence port for the customer aggregate. */
public interface CustomerRepository {
  /** Persists a customer together with its account. */
  Customer save(Customer customer);

  /** Finds a customer by their unique username. */
  Optional<Customer> findByUsername(String username);

  /** Finds a customer aggregate by its identifier. */
  Optional<Customer> findById(Long id);

  /** Returns all customer aggregates. */
  Iterable<Customer> findAll();

  /** Returns the number of registered customers. */
  long count();
}
