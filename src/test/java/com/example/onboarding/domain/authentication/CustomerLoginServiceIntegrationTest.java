package com.example.onboarding.domain.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.onboarding.domain.account.Iban;
import com.example.onboarding.domain.account.IbanGenerator;
import com.example.onboarding.domain.customer.Address;
import com.example.onboarding.domain.exception.InvalidCredentialsException;
import com.example.onboarding.domain.registration.CustomerRegistrationService;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerCommand;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CustomerLoginServiceIntegrationTest {
  private static final Iban IBAN = Iban.dutch("BANK", "0417164300");

  @Autowired private CustomerRegistrationService registrationService;
  @Autowired private CustomerLoginService loginService;
  @Autowired private SessionStore sessionStore;
  @Autowired private Clock clock;
  @MockitoBean private IbanGenerator ibanGenerator;

  @Test
  void shouldLoginWithTheGeneratedDefaultPassword() {
    when(ibanGenerator.generate()).thenReturn(IBAN);
    var registration = registrationService.register(registrationCommand());

    var login =
        loginService.login(
            new CustomerLoginService.LoginCommand("john.doe", registration.password()));

    assertThat(login.accessToken()).isNotBlank();
    assertThat(login.expiresAt()).isAfter(Instant.now(clock));
    assertThat(sessionStore.findByToken(login.accessToken()))
        .isPresent()
        .get()
        .satisfies(
            session -> {
              assertThat(session.customerId()).isNotNull();
              assertThat(session.username()).isEqualTo("john.doe");
            });
  }

  @Test
  void shouldReturnErrorForUnknownUsernameAndWrongPassword() {
    when(ibanGenerator.generate()).thenReturn(IBAN);
    registrationService.register(registrationCommand());

    assertThatThrownBy(
            () ->
                loginService.login(
                    new CustomerLoginService.LoginCommand("john.doe", "wrong-password")))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid username or password");

    assertThatThrownBy(
            () ->
                loginService.login(
                    new CustomerLoginService.LoginCommand("unknown", "wrong-password")))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Invalid username or password");
  }

  private static RegisterCustomerCommand registrationCommand() {
    return new RegisterCustomerCommand(
        "john.doe",
        "John Doe",
        new Address("NL", "Keizersgracht", "123A", "1015 CJ"),
        LocalDate.of(1990, 5, 10));
  }
}
