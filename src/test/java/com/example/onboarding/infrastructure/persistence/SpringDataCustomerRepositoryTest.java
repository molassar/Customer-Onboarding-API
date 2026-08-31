package com.example.onboarding.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.onboarding.config.JdbcConfiguration;
import com.example.onboarding.domain.account.Account;
import com.example.onboarding.domain.account.AccountType;
import com.example.onboarding.domain.account.CurrencyCode;
import com.example.onboarding.domain.account.Iban;
import com.example.onboarding.domain.customer.Address;
import com.example.onboarding.domain.customer.Customer;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;

@DataJdbcTest
@Import(JdbcConfiguration.class)
class SpringDataCustomerRepositoryTest {
  @Autowired private SpringDataCustomerRepository customerRepository;

  @Test
  void shouldSaveCustomerAndGenerateId() {
    var customer = customer();

    var saved = customerRepository.save(customer);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getAccount().getId()).isNotNull();
  }

  @Test
  void shouldFindCustomerById() {
    var customer = customer();

    var saved = customerRepository.save(customer);

    var result = customerRepository.findById(saved.getId());

    assertThat(result).isPresent();

    var loaded = result.orElseThrow();

    assertThat(loaded.getUsername()).isEqualTo("john.doe");
    assertThat(loaded.getPasswordHash()).isEqualTo("password-hash");
    assertThat(loaded.getFullName()).isEqualTo("John Doe");
    assertThat(loaded.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 10));
    assertThat(loaded.getAddress())
        .isEqualTo(new Address("NL", "Keizersgracht", "123A", "1015 CJ"));
    assertThat(loaded.getAccount())
        .satisfies(
            account -> {
              assertThat(account.getIban()).isEqualTo(new Iban("NL04BANK0417164300"));
              assertThat(account.getAccountType()).isEqualTo(AccountType.PRIVATE);
              assertThat(account.getBalance()).isEqualByComparingTo("0.00");
              assertThat(account.getCurrency()).isEqualTo(CurrencyCode.EUR);
            });
  }

  @Test
  void shouldFindCustomerByUsername() {
    var customer = customer();
    customerRepository.save(customer);

    assertThat(customerRepository.findByUsername("john.doe"))
        .isPresent()
        .get()
        .extracting(Customer::getUsername)
        .isEqualTo("john.doe");
  }

  private static Customer customer() {
    return Customer.register(
        "john.doe",
        "password-hash",
        "John Doe",
        LocalDate.of(1990, 5, 10),
        new Address("NL", "Keizersgracht", "123A", "1015 CJ"),
        Account.open(new Iban("NL04BANK0417164300")));
  }
}
