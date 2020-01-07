package com.mitrais.training.atmsimulation.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mitrais.training.atmsimulation.model.Account;
import com.mitrais.training.atmsimulation.model.Transaction;
import com.mitrais.training.atmsimulation.repository.AccountRepository;
import com.mitrais.training.atmsimulation.repository.TransactionRepository;
import com.mitrais.training.atmsimulation.util.TransactionType;

@Controller
public class TransactionController {

    private static final Pattern DIGIT_6_REGEX = Pattern.compile("^\\d{6}$");

    @Autowired
    AccountRepository accountRepo;

    @Autowired
    TransactionRepository transactionRepo;

    @GetMapping("/transaction")
    public String transaction() {
        return "transaction";
    }

    @PostMapping("/transaction")
    public String selectTransaction(@RequestParam(value = "option", required = false) String option, Model model,
            Authentication authentication) {
        String view;
        switch(option) {
        case "1":
            view = "withdraw";
            break;
        case "2":
            view = "transfer1";
            break;
        case "3":
            latestTransactions(authentication.getName(), model);
            view = "last-transactions";
            break;
        case "4":
            view = "transactions-ondate";
            break;
        default:
            view = "redirect:/logout";
        }

        return view;
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam(value = "option", required = false) String option, Model model,
            Authentication authentication) {
        BigDecimal amount;
        switch (option) {
        case "1":
            amount = new BigDecimal(10);
            break;
        case "2":
            amount = new BigDecimal(50);
            break;
        case "3":
            amount = new BigDecimal(100);
            break;
        case "4":
            return "/withdraw-other";
        default:
            return "/transaction";
        }

        Account account = accountRepo.findById(authentication.getName()).get();
        if (amount.compareTo(account.getBalance()) > 0) {
            model.addAttribute("errorMessage", "Insufficient balance $" + amount);
            return "withdraw";
        }

        Transaction transaction = createTransaction(account, TransactionType.WITHDRAW, amount.negate(), null, null);
        model.addAttribute("transaction", transaction);

        return "withdraw-summary";
    }

    @PostMapping("/withdraw-other")
    public String withdrawOther(@RequestParam(value = "amount", required = false) String amountStr, Model model,
            Authentication authentication) {
        int amountInt = 0;
        boolean error = false;
        try {
            amountInt = Integer.parseInt(amountStr);
        } catch (NumberFormatException ex) {
            model.addAttribute("errorMessage", "Invalid ammount");
            error = true;
        }

        if (amountInt > 1000) {
            model.addAttribute("errorMessage", "Maximum amount to withdraw is $1000");
            error = true;
        } else if ((amountInt % 10) > 0) {
            model.addAttribute("errorMessage", "Invalid ammount");
            error = true;
        }

        if (error) {
            return "withdraw-other";
        }

        BigDecimal amount = new BigDecimal(amountInt);
        String accountNumber = authentication.getName();
        Account account = accountRepo.findById(accountNumber).get();
        if (amount.compareTo(account.getBalance()) > 0) {
            model.addAttribute("errorMessage", "Insufficient balance $" + amount);
            return "withdraw-other";
        }

        Transaction transaction = createTransaction(account, TransactionType.WITHDRAW, amount.negate(), null, null);
        model.addAttribute("transaction", transaction);

        return "withdraw-summary";
    }

    @PostMapping("/summary")
    public String summary(@RequestParam(value = "option", required = false) String option) {
        switch (option) {
        case "1":
            return "transaction";
        default:
            return "redirect:/logout";
        }
    }

    @PostMapping("/transfer1")
    public String transfer1(@RequestParam(value = "destAccount", required = false) String destAccount,
            HttpSession session, Model model, Authentication authentication) {
        Optional<Account> result = accountRepo.findById(destAccount);
        if (!result.isPresent() || destAccount.equals(authentication.getName())) {
            model.addAttribute("errorMessage", "Invalid account");
            return "transfer1";
        }

        Transaction transaction = new Transaction();
        transaction.setDestinationAccount(destAccount);
        session.setAttribute("transfer", transaction);

        return "transfer2";
    }

    @PostMapping("/transfer2")
    public String transfer2(@RequestParam(value = "amount", required = false) String amountStr, HttpSession session,
            Model model, Authentication authentication) {

        BigDecimal amount;

        try {
            amount = new BigDecimal(amountStr);

            if (amount.compareTo(new BigDecimal(1000)) > 0) {
                model.addAttribute("errorMessage", "Maximum amount to withdraw is $1000");
                return "transfer2";
            } else if (amount.compareTo(BigDecimal.ONE) < 0) {
                model.addAttribute("errorMessage", "Minimum amount to withdraw is $1");
                return "transfer2";
            }

            Account account = accountRepo.findById(authentication.getName()).get();
            if (amount.compareTo(account.getBalance()) > 0) {
                model.addAttribute("errorMessage", "Insufficient balance $" + amount);
                return "transfer2";
            }
        } catch (NumberFormatException ex) {
            model.addAttribute("errorMessage", "Invalid amount");
            return "transfer2";
        }

        ((Transaction) session.getAttribute("transfer")).setAmount(amount);

        return "transfer3";
    }

    @PostMapping("/transfer3")
    public String transfer3(@RequestParam(value = "refNumber", required = false) String refNumber, HttpSession session,
            Model model) {

        if (refNumber == null || refNumber.length() == 0 || !DIGIT_6_REGEX.matcher(refNumber).matches()) {
            model.addAttribute("errorMessage", "Invalid Reference Number");
            return "transfer3";
        }

        ((Transaction) session.getAttribute("transfer")).setReference(refNumber);

        return "transfer4";
    }

    @PostMapping("/transfer4")
    public String transfer4(@RequestParam(value = "option", required = false) String option, HttpSession session,
            Model model, Authentication authentication) {

        switch (option) {
        case "1":
            Transaction transfer = (Transaction) session.getAttribute("transfer");
            Account account = accountRepo.findById(authentication.getName()).get();
            Account destination = accountRepo.findById(transfer.getDestinationAccount()).get();
            Transaction transaction = createTransaction(account, TransactionType.TRANSFER,
                    transfer.getAmount().negate(), destination, transfer.getReference());
            model.addAttribute("transaction", transaction);

            return "transfer-summary";
        default:
            return "transaction";
        }
    }

    @PostMapping("/transactions-ondate")
    public String transactionsOnDate(@RequestParam(value = "onDate", required = false) String onDate, Model model, Authentication authentication) {
        try {
            LocalDate localDate = LocalDate.parse(onDate);
            Page<Transaction> pagedResult = transactionRepo.findTransactionByAccountOnDate(authentication.getName(),
                    Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(localDate.plusDays(1).atStartOfDay().minusNanos(1).atZone(ZoneId.systemDefault())
                            .toInstant()),
                    PageRequest.of(0, 10, Sort.by("transactionDate").descending()));
            model.addAttribute("transactions", pagedResult.getContent());
        } catch(DateTimeParseException ex) {
            model.addAttribute("errorMessage", "Invalid date format");
            return "transactions-ondate";
        }

        return "transactions-ondate-summary";
    }

    private void latestTransactions(String accountNumber, Model model) {
        Page<Transaction> pagedResult = transactionRepo.findTransactionByAccountNumber(accountNumber,
                PageRequest.of(0, 10, Sort.by("transactionDate").descending()));
        model.addAttribute("transactions", pagedResult.getContent());
    }

    private Transaction createTransaction(Account account, TransactionType type, BigDecimal amount,
            Account destinationAccount, String reference) {
        Transaction transaction = new Transaction();
        transaction.setTransactionDate(new Date());
        transaction.setAccountNumber(account.getAccountNumber());
        transaction.setType(type.toString());
        transaction.setReference(reference);

        if (type == TransactionType.TRANSFER && destinationAccount != null) {
            transaction.setDestinationAccount(destinationAccount.getAccountNumber());
        }

        transaction.setAmount(amount);

        BigDecimal balance = account.getBalance().add(amount);
        transaction.setBalance(balance);
        transactionRepo.save(transaction);

        account.setBalance(balance);
        accountRepo.save(account);

        if (transaction.getDestinationAccount() != null) {
            Transaction inverseTrans = transaction.inverse();
            BigDecimal balance2 = destinationAccount.getBalance().add(inverseTrans.getAmount());
            inverseTrans.setBalance(balance2);
            transactionRepo.save(inverseTrans);

            destinationAccount.setBalance(balance2);
            accountRepo.save(destinationAccount);
        }

        return transaction;
    }
}
