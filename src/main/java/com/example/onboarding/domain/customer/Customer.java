package com.example.onboarding.domain.customer;

import com.example.onboarding.domain.account.Account;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;

/** Aggregate root containing a registered customer's identity, address, and account. */
public class Customer {
  @Id private Long id;

  private final String username;
  private final String passwordHash;
  private final String fullName;
  private final LocalDate dateOfBirth;

  @MappedCollection(idColumn = "CUSTOMER_ID")
  private final Account account;

  @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
  private final Address address;

  public Customer(
      Long id,
      String username,
      String passwordHash,
      String fullName,
      LocalDate dateOfBirth,
      Address address,
      Account account) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.fullName = fullName;
    this.dateOfBirth = dateOfBirth;
    this.address = address;
    this.account = account;
  }

  /** Creates a new, unpersisted customer aggregate ready for registration. */
  public static Customer register(
      String username,
      String passwordHash,
      String fullName,
      LocalDate dateOfBirth,
      Address address,
      Account account) {
    return new Customer(null, username, passwordHash, fullName, dateOfBirth, address, account);
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getFullName() {
    return fullName;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public Address getAddress() {
    return address;
  }

  public Account getAccount() {
    return account;
  }
}
