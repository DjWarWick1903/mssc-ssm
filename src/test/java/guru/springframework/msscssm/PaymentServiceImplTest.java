package guru.springframework.msscssm;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@SpringBootTest
public class PaymentServiceImplTest {
    @Autowired
    PaymentServiceImpl paymentService;
    @Autowired
    PaymentRepository paymentRepository;
    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .amount(new BigDecimal("12.99"))
                .build();
    }

    @Transactional
    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        System.out.println("Should be NEW");
        System.out.println(savedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        System.out.println("Should be PRE_AUTH or PRE_AUTH_ERROR");
        System.out.println(savedPayment.getState());

        System.out.println(preAuthPayment);
    }

    @Transactional
    @RepeatedTest(10)
    void testAuth() {
        Payment savedPayment = paymentService.newPayment(payment);

        StateMachine<PaymentState, PaymentEvent> preAuthSM = paymentService.preAuth(savedPayment.getId());

        if(preAuthSM.getState().getId() == PaymentState.PRE_AUTH) {
            System.out.println("Payment is Pre authorized");

            StateMachine<PaymentState, PaymentEvent> authSM = paymentService.authorizePayment(savedPayment.getId());

            System.out.println("Result of Auth: " + authSM.getState().getId());
        } else {
            System.out.println("Payment failed pre-auth...");
        }
    }
}
