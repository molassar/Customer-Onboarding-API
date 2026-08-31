package com.example.onboarding.infrastructure.persistence;

import com.example.onboarding.domain.customer.Customer;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

interface SpringDataCustomerRepository extends CrudRepository<Customer, Long> {
  Optional<Customer> findByUsername(String username);
}
