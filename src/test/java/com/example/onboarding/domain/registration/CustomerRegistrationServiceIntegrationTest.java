package com.example.onboarding.domain.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.onboarding.domain.account.Iban;
import com.example.onboarding.domain.account.IbanGenerator;
import com.example.onboarding.domain.customer.Address;
import com.example.onboarding.domain.customer.CustomerRepository;
import com.example.onboarding.domain.exception.IbanAlreadyExistsException;
import com.example.onboarding.domain.exception.UnderageCustomerException;
import com.example.onboarding.domain.exception.UnsupportedCountryException;
import com.example.onboarding.domain.exception.UsernameAlreadyExistsException;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerCommand;
import java.time.Clock;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CustomerRegistrationServiceIntegrationTest {
  private static final Iban FIRST_IBAN = Iban.dutch("BANK", "0417164300");
  private static final Iban SECOND_IBAN = Iban.dutch("BANK", "0417164301");

  @Autowired private CustomerRegistrationService customerRegistrationService;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private Clock clock;
  @MockitoBean private IbanGenerator ibanGenerator;

  @Test
  void shouldReturnGeneratedPasswordAndStoreOnlyItsHash() {
    when(ibanGenerator.generate()).thenReturn(FIRST_IBAN);

    var command =
        new RegisterCustomerCommand(
            "jane.doe",
            "Jane Doe",
            new Address("BE", "Nieuwstraat", "10", "1000"),
            LocalDate.of(1992, 8, 15));

    var result = customerRegistrationService.register(command);

    assertThat(result.username()).isEqualTo("jane.doe");
    assertThat(result.password()).isNotBlank();
    assertThat(customerRepository.findAll())
        .singleElement()
        .satisfies(
            customer -> {
              assertThat(customer.getAccount().getIban()).isEqualTo(FIRST_IBAN);
              assertThat(customer.getPasswordHash()).isNotEqualTo(result.password());
              assertThat(passwordEncoder.matches(result.password(), customer.getPasswordHash()))
                  .isTrue();
            });
  }

  @Test
  void shouldRejectDuplicateUsername() {
    when(ibanGenerator.generate()).thenReturn(FIRST_IBAN, SECOND_IBAN);

    var first =
        new RegisterCustomerCommand(
            "john.doe",
            "John Doe",
            new Address("NL", "Keizersgracht", "123A", "1015 CJ"),
            LocalDate.of(1990, 5, 10));

    var second =
        new RegisterCustomerCommand(
            "john.doe",
            "Another John",
            new Address("NL", "Prinsengracht", "42", "1015 DV"),
            LocalDate.of(1985, 3, 20));

    customerRegistrationService.register(first);

    assertThatThrownBy(() -> customerRegistrationService.register(second))
        .isInstanceOf(UsernameAlreadyExistsException.class)
        .hasMessageContaining("john.doe");
  }

  @Test
  void shouldPropagateGeneratedIbanCollision() {
    when(ibanGenerator.generate()).thenReturn(FIRST_IBAN);

    var first =
        new RegisterCustomerCommand(
            "john.doe",
            "John Doe",
            new Address("NL", "Keizersgracht", "123A", "1015 CJ"),
            LocalDate.of(1990, 5, 10));

    var second =
        new RegisterCustomerCommand(
            "jane.doe",
            "Jane Doe",
            new Address("NL", "Herengracht", "10", "1015 BS"),
            LocalDate.of(1992, 8, 15));

    customerRegistrationService.register(first);

    assertThatThrownBy(() -> customerRegistrationService.register(second))
        .isInstanceOf(IbanAlreadyExistsException.class)
        .hasMessageContaining(FIRST_IBAN.value());
  }

  @Test
  void shouldRejectCustomerOutsideNetherlandsAndBelgium() {
    var command =
        new RegisterCustomerCommand(
            "hans.schmidt",
            "Hans Schmidt",
            new Address("DE", "Hauptstrasse", "10", "10115"),
            LocalDate.of(1990, 5, 10));

    assertThatThrownBy(() -> customerRegistrationService.register(command))
        .isInstanceOf(UnsupportedCountryException.class)
        .hasMessageContaining("BE")
        .hasMessageContaining("NL")
        .hasMessageContaining("DE");

    assertThat(customerRepository.count()).isZero();
    verifyNoInteractions(ibanGenerator);
  }

  @Test
  void shouldRejectUnderageCustomer() {
    var command =
        new RegisterCustomerCommand(
            "young.customer",
            "Young Customer",
            new Address("NL", "Keizersgracht", "10", "1015 CJ"),
            LocalDate.now(clock).minusYears(18).plusDays(1));

    assertThatThrownBy(() -> customerRegistrationService.register(command))
        .isInstanceOf(UnderageCustomerException.class)
        .hasMessageContaining("18 years old");

    assertThat(customerRepository.count()).isZero();
    verifyNoInteractions(ibanGenerator);
  }
}
