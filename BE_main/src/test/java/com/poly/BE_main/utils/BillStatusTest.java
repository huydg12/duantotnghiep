package com.poly.BE_main.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BillStatusTest {
    @Test void allowsNormalFulfillmentFlow() {
        assertTrue(BillStatus.canTransition(1, 2));
        assertTrue(BillStatus.canTransition(2, 3));
        assertTrue(BillStatus.canTransition(3, 4));
    }
    @Test void rejectsSkippingAndReopeningTerminalStates() {
        assertFalse(BillStatus.canTransition(1, 4));
        assertFalse(BillStatus.canTransition(5, 1));
        assertFalse(BillStatus.canTransition(4, 2));
    }
    @Test void permitsCancellationOnlyBeforeShipping() {
        assertTrue(BillStatus.canTransition(1, 5));
        assertTrue(BillStatus.canTransition(2, 5));
        assertFalse(BillStatus.canTransition(3, 5));
    }
}
