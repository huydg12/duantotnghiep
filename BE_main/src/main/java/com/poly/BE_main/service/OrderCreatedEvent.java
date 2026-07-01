package com.poly.BE_main.service;
import java.math.BigDecimal;
public record OrderCreatedEvent(String email, String billCode, BigDecimal total) {}
