package com.example.onboarding.api;

import com.example.onboarding.api.authentication.BearerTokenAuthenticator;
import com.example.onboarding.api.dto.AccountOverviewResponse;
import com.example.onboarding.api.dto.LoginRequest;
import com.example.onboarding.api.dto.LoginResponse;
import com.example.onboarding.api.dto.RegisterRequest;
import com.example.onboarding.api.dto.RegisterResponse;
import com.example.onboarding.domain.account.AccountOverviewService;
import com.example.onboarding.domain.authentication.CustomerLoginService;
import com.example.onboarding.domain.authentication.CustomerLoginService.LoginCommand;
import com.example.onboarding.domain.customer.Address;
import com.example.onboarding.domain.registration.CustomerRegistrationService;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerCommand;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** HTTP API for customer registration, authentication, and account overview. */
@RestController
public class CustomerOnboardingController {
  private final CustomerRegistrationService customerRegistrationService;
  private final CustomerLoginService customerLoginService;
  private final BearerTokenAuthenticator bearerTokenAuthenticator;
  private final AccountOverviewService accountOverviewService;

  public CustomerOnboardingController(
      CustomerRegistrationService customerRegistrationService,
      CustomerLoginService customerLoginService,
      BearerTokenAuthenticator bearerTokenAuthenticator,
      AccountOverviewService accountOverviewService) {
    this.customerRegistrationService = customerRegistrationService;
    this.customerLoginService = customerLoginService;
    this.bearerTokenAuthenticator = bearerTokenAuthenticator;
    this.accountOverviewService = accountOverviewService;
  }

  /**
   * Registers a customer and opens their default account.
   *
   * @return the username and generated initial password
   */
  @PostMapping("register")
  public RegisterResponse registerCustomer(@Valid @RequestBody RegisterRequest request) {
    Address address =
        new Address(
            request.address().countryCode(),
            request.address().street(),
            request.address().houseNumber(),
            request.address().postalCode());

    RegisterCustomerResult result =
        customerRegistrationService.register(
            new RegisterCustomerCommand(
                request.username(), request.fullName(), address, request.dateOfBirth()));

    return new RegisterResponse(result.username(), result.password());
  }

  /**
   * Authenticates a customer using their username and password.
   *
   * @return a bearer token and its expiration time
   */
  @PostMapping("login")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    var result =
        customerLoginService.login(new LoginCommand(request.username(), request.password()));
    return new LoginResponse(result.accessToken(), result.expiresAt());
  }

  /**
   * Returns the account overview for the customer identified by the bearer token.
   *
   * @param authorizationHeader an HTTP {@code Authorization: Bearer <token>} header
   */
  @GetMapping("overview")
  public AccountOverviewResponse overview(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
          String authorizationHeader) {
    var session = bearerTokenAuthenticator.authenticate(authorizationHeader);
    var overview = accountOverviewService.getOverview(session.customerId());

    return new AccountOverviewResponse(
        overview.accountNumber().value(),
        overview.accountType(),
        overview.balance(),
        overview.currency());
  }
}
