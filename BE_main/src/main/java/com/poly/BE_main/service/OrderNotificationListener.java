package com.poly.BE_main.service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
public class OrderNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);
    private final EmailService emailService;
    public OrderNotificationListener(EmailService emailService) { this.emailService = emailService; }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            emailService.sendOrderConfirmation(event.email(), event.billCode(), event.total());
        } catch (RuntimeException ex) {
            log.error("Order {} was committed but confirmation email failed", event.billCode(), ex);
        }
    }
}
