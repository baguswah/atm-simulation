package com.mitrais.training.atmsimulation;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AtmSimulationTransactionTest {

    @Autowired
    private MockMvc mockMvc;

    @WithMockUser(value = "112233")
    @Test
    public void withdrawSufficientAmountShouldSuccess() throws Exception {
        this.mockMvc.perform(post("/withdraw").param("option", "1")).andDo(print())
            .andExpect(status().isOk())
                .andExpect(content().string(both(containsString("Withdraw : $<span>10</span>"))
                        .and(containsString("Balance : $<span>90</span>"))));
    }

    @WithMockUser(value = "112233")
    @Test
    public void withdrawInsufficientAmountShouldError() throws Exception {
        this.mockMvc.perform(post("/withdraw-other").param("amount", "110")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Insufficient balance $110")));
    }

    @WithMockUser(value = "112233")
    @Test
    public void transferToNonExistsAccountShouldError() throws Exception {
        this.mockMvc.perform(post("/transfer1").param("destAccount", "000001")).andDo(print())
                .andExpect(status().isOk()).andExpect(content().string(containsString("Invalid account")));
    }

    @WithMockUser(value = "112233")
    @Test
    public void transferWithInsufficientAmountShouldError() throws Exception {
        this.mockMvc.perform(post("/transfer1").param("destAccount", "785050")).andExpect(status().isOk());
        this.mockMvc.perform(post("/transfer2").param("amount", "200")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("Insufficient balance $200")));
    }

    @WithMockUser(value = "601455")
    @Test
    public void withdrawAndlast10TransactionsShouldExists() throws Exception {
        this.mockMvc.perform(post("/withdraw-other").param("amount", "170")).andExpect(status().isOk());
        this.mockMvc.perform(post("/transaction").param("option", "3")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("<td>170</td>")));
    }

    @WithMockUser(value = "572772")
    @Test
    public void doTransactionsAndlast10TransactionsShouldExists() throws Exception {
        this.mockMvc.perform(post("/withdraw-other").param("amount", "100")).andExpect(status().isOk());
        this.mockMvc.perform(post("/withdraw-other").param("amount", "200")).andExpect(status().isOk());
        this.mockMvc.perform(post("/withdraw-other").param("amount", "300")).andExpect(status().isOk());
        this.mockMvc.perform(post("/withdraw-other").param("amount", "400")).andExpect(status().isOk());
        this.mockMvc.perform(post("/transaction").param("option", "3")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("<td>1999.17</td>")));
    }
}
