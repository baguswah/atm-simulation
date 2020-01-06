package com.mitrais.training.atmsimulation.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mitrais.training.atmsimulation.model.Account;
import com.mitrais.training.atmsimulation.repository.AccountRepository;

public class AccountAuthenticationProvider implements AuthenticationProvider {
    private static final int     ACCOUNT_NUMBER_LENGTH = 6;
    private static final int     PIN_LENGTH            = 6;
    private static final Pattern DIGIT_6_REGEX         = Pattern.compile("^\\d{6}$");

    private AccountRepository accountRepo;

    public AccountAuthenticationProvider(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        String accountNumber = token.getName() != null ? token.getName() : "";
        if (accountNumber.length() != ACCOUNT_NUMBER_LENGTH) {
            throw new UsernameNotFoundException("Account Number should have 6 digits length");
        }

        if (!DIGIT_6_REGEX.matcher(accountNumber).matches()) {
            throw new UsernameNotFoundException("Account Number should only contains numbers");
        }

        String pin = token.getCredentials() != null ? token.getCredentials().toString() : "";
        if (pin.length() != PIN_LENGTH) {
            throw new UsernameNotFoundException("PIN should have 6 digits length");
        }

        if (!DIGIT_6_REGEX.matcher(pin).matches()) {
            throw new UsernameNotFoundException("PIN should only contains numbers");
        }

        Account account = accountRepo.findByAccountNumberAndPin(accountNumber, pin);
        if (account == null) {
            throw new UsernameNotFoundException("Invalid Account Number/PIN");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("account"));
        return new UsernamePasswordAuthenticationToken(accountNumber, pin, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
