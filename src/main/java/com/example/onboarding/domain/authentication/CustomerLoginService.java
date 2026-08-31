package com.example.onboarding.domain.authentication;

import com.example.onboarding.domain.customer.Customer;
import com.example.onboarding.domain.customer.CustomerRepository;
import com.example.onboarding.domain.exception.InvalidCredentialsException;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Verifies customer credentials and starts authenticated sessions. */
@Service
public class CustomerLoginService {
  private final CustomerRepository customerRepository;
  private final PasswordEncoder passwordEncoder;
  private final SessionStore sessionStore;

  public CustomerLoginService(
      CustomerRepository customerRepository,
      PasswordEncoder passwordEncoder,
      SessionStore sessionStore) {
    this.customerRepository = customerRepository;
    this.passwordEncoder = passwordEncoder;
    this.sessionStore = sessionStore;
  }

  /**
   * Authenticates the supplied credentials and creates an expiring access token.
   *
   * @throws InvalidCredentialsException when the username or password is incorrect
   */
  public LoginResult login(LoginCommand command) {
    Customer customer =
        customerRepository
            .findByUsername(command.username())
            .filter(found -> passwordEncoder.matches(command.password(), found.getPasswordHash()))
            .orElseThrow(InvalidCredentialsException::new);

    AuthenticatedSession session = sessionStore.create(customer.getId(), customer.getUsername());

    return new LoginResult(session.accessToken(), session.expiresAt());
  }

  public record LoginCommand(String username, String password) {}

  public record LoginResult(String accessToken, Instant expiresAt) {}
}
