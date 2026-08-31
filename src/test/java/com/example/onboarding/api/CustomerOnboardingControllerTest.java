package com.example.onboarding.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onboarding.api.authentication.BearerTokenAuthenticator;
import com.example.onboarding.api.exceptionhandling.ApiExceptionHandler;
import com.example.onboarding.domain.account.AccountOverview;
import com.example.onboarding.domain.account.AccountOverviewService;
import com.example.onboarding.domain.account.AccountType;
import com.example.onboarding.domain.account.CurrencyCode;
import com.example.onboarding.domain.account.Iban;
import com.example.onboarding.domain.authentication.AuthenticatedSession;
import com.example.onboarding.domain.authentication.CustomerLoginService;
import com.example.onboarding.domain.authentication.CustomerLoginService.LoginCommand;
import com.example.onboarding.domain.authentication.CustomerLoginService.LoginResult;
import com.example.onboarding.domain.exception.IbanAlreadyExistsException;
import com.example.onboarding.domain.exception.InvalidCredentialsException;
import com.example.onboarding.domain.exception.UnderageCustomerException;
import com.example.onboarding.domain.exception.UnauthenticatedException;
import com.example.onboarding.domain.exception.UnsupportedCountryException;
import com.example.onboarding.domain.registration.CustomerRegistrationService;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerCommand;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerResult;
import com.example.onboarding.infrastructure.exception.DatabaseRateLimitExceededException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerOnboardingController.class)
@Import(ApiExceptionHandler.class)
class CustomerOnboardingControllerTest {
  @Autowired MockMvc mockMvc;

  @MockitoBean CustomerRegistrationService customerRegistrationService;
  @MockitoBean CustomerLoginService customerLoginService;
  @MockitoBean BearerTokenAuthenticator bearerTokenAuthenticator;
  @MockitoBean AccountOverviewService accountOverviewService;

  @Test
  void shouldReturnAccountOverviewForAuthenticatedCustomer() throws Exception {
    var session =
        new AuthenticatedSession(
            "access-token", Instant.parse("2026-08-31T13:00:00Z"), 42L, "john.doe");
    when(bearerTokenAuthenticator.authenticate("Bearer access-token")).thenReturn(session);
    when(accountOverviewService.getOverview(42L))
        .thenReturn(
            new AccountOverview(
                new Iban("NL04BANK0417164300"),
                AccountType.PRIVATE,
                new BigDecimal("0.00"),
                CurrencyCode.EUR));

    mockMvc
        .perform(get("/overview").header("Authorization", "Bearer access-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("NL04BANK0417164300"))
        .andExpect(jsonPath("$.accountType").value("PRIVATE"))
        .andExpect(jsonPath("$.balance").value(0.00))
        .andExpect(jsonPath("$.currency").value("EUR"));

    verify(accountOverviewService).getOverview(42L);
  }

  @Test
  void shouldReturnUnauthorizedWhenOverviewAuthenticationIsMissing() throws Exception {
    when(bearerTokenAuthenticator.authenticate(null)).thenThrow(new UnauthenticatedException());

    mockMvc
        .perform(get("/overview"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title").value("Authentication required"))
        .andExpect(jsonPath("$.detail").value("A valid bearer token is required"));

    verifyNoInteractions(accountOverviewService);
  }

  @Test
  void shouldLoginCustomerAndReturnAccessToken() throws Exception {
    var expiresAt = Instant.parse("2026-08-31T13:00:00Z");
    when(customerLoginService.login(any(LoginCommand.class)))
        .thenReturn(new LoginResult("opaque-access-token", expiresAt));

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "  JOHN.DOE  ",
                      "password": "generated-password"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("opaque-access-token"))
        .andExpect(jsonPath("$.expiresAt").value("2026-08-31T13:00:00Z"));

    var captor = ArgumentCaptor.forClass(LoginCommand.class);
    verify(customerLoginService).login(captor.capture());
    assertThat(captor.getValue().username()).isEqualTo("john.doe");
    assertThat(captor.getValue().password()).isEqualTo("generated-password");
  }

  @Test
  void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
    when(customerLoginService.login(any(LoginCommand.class)))
        .thenThrow(new InvalidCredentialsException());

    mockMvc
        .perform(
            post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "john.doe",
                      "password": "wrong-password"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title").value("Authentication failed"))
        .andExpect(jsonPath("$.detail").value("Invalid username or password"));
  }

  @Test
  void shouldRegisterCustomer() throws Exception {
    when(customerRegistrationService.register(any(RegisterCustomerCommand.class)))
        .thenReturn(new RegisterCustomerResult("john.doe", "generated-password"));

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {
                                          "username": "john.doe",
                                          "fullName": "John Doe",
                                          "dateOfBirth": "1990-05-10",
                                          "address": {
                                            "countryCode": "NL",
                                            "street": "Keizersgracht",
                                            "houseNumber": "123A",
                                            "postalCode": "1015 CJ"
                                          }
                                        }
                                        """))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.username").value("john.doe"))
        .andExpect(jsonPath("$.password").value("generated-password"));
  }

  @Test
  void shouldNormalizeRequestBeforePassingItToService() throws Exception {
    when(customerRegistrationService.register(any(RegisterCustomerCommand.class)))
        .thenReturn(new RegisterCustomerResult("john.doe", "password"));

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                      "username": "  JOHN.DOE  ",
                                      "fullName": "  John Doe  ",
                                      "dateOfBirth": "1990-05-10",
                                      "address": {
                                        "countryCode": " nl ",
                                        "street": "  Keizersgracht  ",
                                        "houseNumber": " 123a ",
                                        "postalCode": " 1015 cj "
                                      }
                                    }
                                    """))
        .andExpect(status().isOk());

    var captor = ArgumentCaptor.forClass(RegisterCustomerCommand.class);

    verify(customerRegistrationService).register(captor.capture());

    var command = captor.getValue();

    assertThat(command.username()).isEqualTo("john.doe");
    assertThat(command.fullName()).isEqualTo("John Doe");
    assertThat(command.dateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 10));

    assertThat(command.address().countryCode()).isEqualTo("NL");
    assertThat(command.address().street()).isEqualTo("Keizersgracht");
    assertThat(command.address().houseNumber()).isEqualTo("123A");
    assertThat(command.address().postalCode()).isEqualTo("1015 CJ");
  }

  @Test
  void shouldReturnBadRequestForBlankUsername() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                      "username": "   ",
                                      "fullName": "John Doe",
                                      "dateOfBirth": "1990-05-10",
                                      "address": {
                                        "countryCode": "NL",
                                        "street": "Keizersgracht",
                                        "houseNumber": "123A",
                                        "postalCode": "1015 CJ"
                                      }
                                    }
                                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.errors.username").exists());

    verifyNoInteractions(customerRegistrationService);
  }

  @Test
  void shouldReturnBadRequestWhenAddressIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                      "username": "john.doe",
                                      "fullName": "John Doe",
                                      "dateOfBirth": "1990-05-10"
                                    }
                                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.address").exists());

    verifyNoInteractions(customerRegistrationService);
  }

  @Test
  void shouldReturnBadRequestForInvalidCountryCode() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                    {
                                      "username": "john.doe",
                                      "fullName": "John Doe",
                                      "dateOfBirth": "1990-05-10",
                                      "address": {
                                        "countryCode": "XX",
                                        "street": "Keizersgracht",
                                        "houseNumber": "123A",
                                        "postalCode": "1015 CJ"
                                      }
                                    }
                                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Invalid request"))
        .andExpect(jsonPath("$.detail").value("Invalid country code: XX"));

    verifyNoInteractions(customerRegistrationService);
  }

  @Test
  void shouldReturnInternalServerErrorWhenIbanGenerationCollides() throws Exception {
    when(customerRegistrationService.register(any(RegisterCustomerCommand.class)))
        .thenThrow(
            new IbanAlreadyExistsException(
                "NL04BANK0417164300", new IllegalStateException("Duplicate IBAN")));

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "john.doe",
                      "fullName": "John Doe",
                      "dateOfBirth": "1990-05-10",
                      "address": {
                        "countryCode": "NL",
                        "street": "Keizersgracht",
                        "houseNumber": "123A",
                        "postalCode": "1015 CJ"
                      }
                    }
                    """))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.title").value("IBAN generation failed"))
        .andExpect(jsonPath("$.detail").value("Could not generate a unique IBAN"));
  }

  @Test
  void shouldReturnBadRequestForUnsupportedCountry() throws Exception {
    when(customerRegistrationService.register(any(RegisterCustomerCommand.class)))
        .thenThrow(new UnsupportedCountryException("DE", Set.of("BE", "NL")));

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "hans.schmidt",
                      "fullName": "Hans Schmidt",
                      "dateOfBirth": "1990-05-10",
                      "address": {
                        "countryCode": "DE",
                        "street": "Hauptstrasse",
                        "houseNumber": "10",
                        "postalCode": "10115"
                      }
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Unsupported country"))
        .andExpect(jsonPath("$.detail").value(containsString("DE")));
  }

  @Test
  void shouldReturnBadRequestForUnderageCustomer() throws Exception {
    when(customerRegistrationService.register(any(RegisterCustomerCommand.class)))
        .thenThrow(new UnderageCustomerException());

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "young.customer",
                      "fullName": "Young Customer",
                      "dateOfBirth": "2020-05-10",
                      "address": {
                        "countryCode": "NL",
                        "street": "Keizersgracht",
                        "houseNumber": "10",
                        "postalCode": "1015 CJ"
                      }
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Customer is underage"))
        .andExpect(
            jsonPath("$.detail")
                .value("Customers must be at least 18 years old to register"));
  }

  @Test
  void shouldReturnTooManyRequestsWhenDatabaseRateLimitIsExceeded() throws Exception {
    when(customerRegistrationService.register(any(RegisterCustomerCommand.class)))
        .thenThrow(new DatabaseRateLimitExceededException());

    mockMvc
        .perform(
            post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "john.doe",
                      "fullName": "John Doe",
                      "dateOfBirth": "1990-05-10",
                      "address": {
                        "countryCode": "NL",
                        "street": "Keizersgracht",
                        "houseNumber": "123A",
                        "postalCode": "1015 CJ"
                      }
                    }
                    """))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.title").value("Too many requests"))
        .andExpect(jsonPath("$.detail").value(containsString("try again later")));
  }
}
