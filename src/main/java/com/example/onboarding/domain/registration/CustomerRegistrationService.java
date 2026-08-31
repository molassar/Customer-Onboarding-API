package com.example.onboarding.domain.registration;

import com.example.onboarding.domain.account.Account;
import com.example.onboarding.domain.account.Iban;
import com.example.onboarding.domain.account.IbanGenerator;
import com.example.onboarding.domain.customer.Address;
import com.example.onboarding.domain.customer.Customer;
import com.example.onboarding.domain.customer.CustomerRepository;
import com.example.onboarding.domain.exception.IbanAlreadyExistsException;
import com.example.onboarding.domain.exception.UnderageCustomerException;
import com.example.onboarding.domain.exception.UnsupportedCountryException;
import com.example.onboarding.domain.exception.UsernameAlreadyExistsException;
import com.example.onboarding.domain.passwordgeneration.PasswordGenerator;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Coordinates customer registration and creation of the customer's initial account. */
@Service
public class CustomerRegistrationService {
  private final CustomerRepository customerRepository;
  private final PasswordGenerator passwordGenerator;
  private final PasswordEncoder passwordEncoder;
  private final IbanGenerator ibanGenerator;
  private final RegistrationProperties registrationProperties;
  private final CustomerAgePolicy customerAgePolicy;

  public CustomerRegistrationService(
      CustomerRepository customerRepository,
      PasswordGenerator passwordGenerator,
      PasswordEncoder passwordEncoder,
      IbanGenerator ibanGenerator,
      RegistrationProperties registrationProperties,
      CustomerAgePolicy customerAgePolicy) {
    this.customerRepository = customerRepository;
    this.passwordGenerator = passwordGenerator;
    this.passwordEncoder = passwordEncoder;
    this.ibanGenerator = ibanGenerator;
    this.registrationProperties = registrationProperties;
    this.customerAgePolicy = customerAgePolicy;
  }

  /**
   * Registers an eligible customer and persists the customer and account as one aggregate.
   *
   * @return the registered username and generated initial password
   * @throws UnsupportedCountryException when the address country is not configured as allowed
   * @throws UnderageCustomerException when the customer is younger than the minimum age
   * @throws UsernameAlreadyExistsException when the username is already registered
   * @throws IbanAlreadyExistsException when the generated IBAN collides with an existing account
   */
  public RegisterCustomerResult register(RegisterCustomerCommand command) {
    String countryCode = command.address().countryCode();
    if (!registrationProperties.allows(countryCode)) {
      throw new UnsupportedCountryException(countryCode, registrationProperties.allowedCountries());
    }
    customerAgePolicy.verify(command.dateOfBirth());

    String password = passwordGenerator.generate();
    String passwordHash = passwordEncoder.encode(password);
    Iban iban = ibanGenerator.generate();
    Account account = Account.open(iban);

    Customer customer =
        Customer.register(
            command.username(),
            passwordHash,
            command.fullName(),
            command.dateOfBirth(),
            command.address(),
            account);

    try {
      customerRepository.save(customer);
    } catch (DuplicateKeyException ex) {
      if (causedByConstraint(ex, "uq_account_iban")) {
        throw new IbanAlreadyExistsException(iban.value(), ex);
      }
      if (causedByConstraint(ex, "uq_customer_username")) {
        throw new UsernameAlreadyExistsException(customer.getUsername(), ex);
      }
      throw ex;
    }

    return new RegisterCustomerResult(customer.getUsername(), password);
  }

  private static boolean causedByConstraint(Throwable exception, String constraintName) {
    String expected = constraintName.toLowerCase(Locale.ROOT);

    for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
      if (cause.getMessage() != null
          && cause.getMessage().toLowerCase(Locale.ROOT).contains(expected)) {
        return true;
      }
    }

    return false;
  }

  public record RegisterCustomerCommand(
      String username, String fullName, Address address, LocalDate dateOfBirth) {}

  public record RegisterCustomerResult(String username, String password) {}
}
