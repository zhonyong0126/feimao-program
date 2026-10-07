package com.feimao;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.feimao.controller.common.TaskCompatibilityController;
import com.feimao.service.InsuranceService;
import com.feimao.service.OrderTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TaskCompatibilityControllerTest {
  private OrderTaskService orderTasks;
  private InsuranceService insurance;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    orderTasks = mock(OrderTaskService.class);
    insurance = mock(InsuranceService.class);
    mvc =
        MockMvcBuilders.standaloneSetup(
                new TaskCompatibilityController(orderTasks, insurance))
            .build();
  }

  @Test
  void exposesTheFourOriginalPostOnlyCronActionsWithEmptyResponses() throws Exception {
    mvc.perform(post("/api/task/tenCancelOrder"))
        .andExpect(status().isOk())
        .andExpect(content().string(""));
    mvc.perform(post("/api/task/leaseOrderUnderway"))
        .andExpect(status().isOk())
        .andExpect(content().string(""));
    mvc.perform(post("/api/task/orderGuestInsure"))
        .andExpect(status().isOk())
        .andExpect(content().string(""));
    mvc.perform(post("/api/task/orderGuestInsureCancel"))
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    verify(orderTasks).cancelUnpaidOrders();
    verify(orderTasks).startPaidLeaseOrders();
    verify(insurance).insurePendingGuests();
    verify(insurance).cancelPendingGuests();
  }

  @Test
  void rejectsGetLikeTheOriginalPostOnlyApiContract() throws Exception {
    mvc.perform(get("/api/task/tenCancelOrder")).andExpect(status().isMethodNotAllowed());
  }
}
