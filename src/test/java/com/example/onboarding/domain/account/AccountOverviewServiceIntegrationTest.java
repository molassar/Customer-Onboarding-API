package com.example.onboarding.domain.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.onboarding.domain.customer.Address;
import com.example.onboarding.domain.customer.CustomerRepository;
import com.example.onboarding.domain.registration.CustomerRegistrationService;
import com.example.onboarding.domain.registration.CustomerRegistrationService.RegisterCustomerCommand;
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
class AccountOverviewServiceIntegrationTest {
  private static final Iban IBAN = Iban.dutch("BANK", "0417164300");

  @Autowired private CustomerRegistrationService registrationService;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private AccountOverviewService accountOverviewService;
  @MockitoBean private IbanGenerator ibanGenerator;

  @Test
  void shouldReturnTheRegisteredCustomersAccountOverview() {
    when(ibanGenerator.generate()).thenReturn(IBAN);
    registrationService.register(
        new RegisterCustomerCommand(
            "john.doe",
            "John Doe",
            new Address("NL", "Keizersgracht", "123A", "1015 CJ"),
            LocalDate.of(1990, 5, 10)));
    var customer = customerRepository.findByUsername("john.doe").orElseThrow();

    var overview = accountOverviewService.getOverview(customer.getId());

    assertThat(overview.accountNumber()).isEqualTo(IBAN);
    assertThat(overview.accountType()).isEqualTo(AccountType.PRIVATE);
    assertThat(overview.balance()).isEqualByComparingTo("0.00");
    assertThat(overview.currency()).isEqualTo(CurrencyCode.EUR);
  }
}
