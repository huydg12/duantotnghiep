package com.poly.BE_main.service;

import com.poly.BE_main.dto.BillDTO;
import com.poly.BE_main.dto.BillDetailDTO;
import com.poly.BE_main.dto.InvoiceCustomerDTO;
import com.poly.BE_main.dto.InvoiceItemCustomerDTO;
import com.poly.BE_main.model.*;
import com.poly.BE_main.repository.*;
import com.poly.BE_main.utils.BillStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillService {
    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;
    private final InventoryRepository inventoryRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ProductDetailRepository productDetailRepository;
    private final PromotionRepository promotionRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final BillStatusHistoryRepository statusHistoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final PromotionUsageRepository promotionUsageRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BillService(BillRepository billRepository, BillDetailRepository billDetailRepository,
                       InventoryRepository inventoryRepository, AccountRepository accountRepository,
                       CustomerRepository customerRepository, ProductDetailRepository productDetailRepository,
                       PromotionRepository promotionRepository, CartRepository cartRepository,
                       CartDetailRepository cartDetailRepository, BillStatusHistoryRepository statusHistoryRepository,
                       InventoryMovementRepository inventoryMovementRepository, PromotionUsageRepository promotionUsageRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.billRepository = billRepository;
        this.billDetailRepository = billDetailRepository;
        this.inventoryRepository = inventoryRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.productDetailRepository = productDetailRepository;
        this.promotionRepository = promotionRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.promotionUsageRepository = promotionUsageRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public Optional<Bill> findById(int id) {
        return billRepository.findById(id);
    }

    public void delete(Integer id) {
        billRepository.deleteById(id);
    }

    @Deprecated
    public Bill create(Bill ignored) {
        throw new ResponseStatusException(HttpStatus.GONE, "Use the secure checkout endpoint");
    }

    @Transactional
    public Bill createBill(BillDTO dto, String username) {
        Account actor = accountRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        Integer customerId = resolveCustomerId(actor, dto.getCustomerId());
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BillDetail> details = new ArrayList<>();

        for (BillDetailDTO line : dto.getBillDetails()) {
            ProductDetail pd = productDetailRepository.findById(line.getProductDetailId())
                    .orElseThrow(() -> badRequest("Product variant not found"));
            if (!pd.isActive() || !pd.getProduct().isActive() || pd.getPrice() == null || pd.getPrice().signum() < 0)
                throw badRequest("Product variant is unavailable");
            Inventory stock = inventoryRepository.lockByProductDetailId(pd.getId())
                    .orElseThrow(() -> badRequest("Inventory not found"));
            if (stock.getQuantity() < line.getQuantity())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient inventory for variant " + pd.getId());
            stock.setQuantity(stock.getQuantity() - line.getQuantity());
            subtotal = subtotal.add(pd.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));

            BillDetail detail = new BillDetail();
            detail.setProductDetailId(pd.getId());
            detail.setQuantity(line.getQuantity());
            detail.setPrice(pd.getPrice());
            detail.setStatus(1);
            detail.setProductName(pd.getProduct().getProductName());
            detail.setColor(pd.getColor().getName());
            detail.setSize(pd.getSize().getEu());
            details.add(detail);
        }

        BigDecimal discount = calculateDiscount(dto.getPromotionId(), subtotal, customerId);
        BigDecimal shipping = actor.getRoleId() == 2 ? calculateShipping(dto.getReceiverAddress()) : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).add(shipping).max(BigDecimal.ZERO);

        Bill bill = new Bill();
        bill.setCustomerId(customerId);
        bill.setEmployeeId(dto.getEmployeeId());
        bill.setPtttId(dto.getPtttId());
        bill.setPromotionId(dto.getPromotionId());
        bill.setCode(newBillCode());
        bill.setBillType(actor.getRoleId() == 2 ? "ONLINE" : "POS");
        bill.setStatus(1);
        bill.setCreatedBy(username);
        bill.setCreatedDate(LocalDate.now());
        bill.setRecipientName(dto.getRecipientName());
        bill.setRecipientPhoneNumber(dto.getRecipientPhoneNumber());
        bill.setReceiverAddress(dto.getReceiverAddress());
        bill.setAddressMethod(dto.getAddressMethod());
        bill.setNote(dto.getNote());
        bill.setStatusPayment(actor.getRoleId() == 2 ? "Chưa thanh toán" : "Đã thanh toán");
        bill.setSubTotal(subtotal);
        bill.setDiscountAmount(discount);
        bill.setShippingFee(shipping);
        bill.setGrandTotal(total);
        Bill saved = billRepository.save(bill);
        details.forEach(d -> d.setBill(saved));
        billDetailRepository.saveAll(details);
        saveStatusHistory(saved.getId(), null, BillStatus.CHO_XAC_NHAN, username, "Tạo đơn hàng");
        for (BillDetail detail : details) {
            Inventory stock = inventoryRepository.findByProductDetailId(detail.getProductDetailId()).orElseThrow();
            saveMovement(detail.getProductDetailId(), -detail.getQuantity(), stock.getQuantity(),
                    "SALE", "BILL", saved.getId(), username);
        }
        if (dto.getPromotionId() != null) {
            PromotionUsage usage = new PromotionUsage();
            usage.setPromotionId(dto.getPromotionId());
            usage.setCustomerId(customerId);
            usage.setBillId(saved.getId());
            usage.setUsedAt(LocalDateTime.now());
            promotionUsageRepository.save(usage);
        }

        if (actor.getRoleId() == 2) {
            Integer cartId = cartRepository.findCartIdByCustomerId(customerId);
            if (cartId != null) cartDetailRepository.deleteAllByCartId(cartId);
        }
        customerRepository.findById(customerId).ifPresent(c ->
                eventPublisher.publishEvent(new OrderCreatedEvent(c.getEmail(), saved.getCode(), saved.getGrandTotal())));
        return saved;
    }

    private Integer resolveCustomerId(Account actor, Integer requested) {
        if (actor.getRoleId() != 2) {
            if (requested == null || !customerRepository.existsById(requested))
                throw badRequest("Customer is required");
            return requested;
        }
        return customerRepository.findByAccountId(actor.getId()).map(Customer::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer profile not found"));
    }

    BigDecimal calculateDiscount(Integer promotionId, BigDecimal subtotal, Integer customerId) {
        if (promotionId == null) return BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        Promotion p = promotionRepository.findByIdAndIsActiveTrue(promotionId)
                .filter(x -> x.getStartDate() != null && x.getEndDate() != null
                        && !now.isBefore(x.getStartDate()) && !now.isAfter(x.getEndDate()))
                .orElseThrow(() -> badRequest("Promotion is invalid or expired"));
        if (p.getMinOrderAmount() != null && subtotal.compareTo(p.getMinOrderAmount()) < 0)
            throw badRequest("Order does not meet the promotion minimum amount");
        if (p.getUsageLimit() != null && promotionUsageRepository.countByPromotionId(promotionId) >= p.getUsageLimit())
            throw badRequest("Promotion usage limit reached");
        if (p.getPerCustomerLimit() != null
                && promotionUsageRepository.countByPromotionIdAndCustomerId(promotionId, customerId) >= p.getPerCustomerLimit())
            throw badRequest("Promotion usage limit reached for this customer");
        BigDecimal value = p.getType() == 1
                ? subtotal.multiply(p.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                : p.getValue();
        value = value.max(BigDecimal.ZERO).min(subtotal);
        return p.getMaxDiscountAmount() == null ? value : value.min(p.getMaxDiscountAmount());
    }

    private BigDecimal calculateShipping(String address) {
        if (address == null || address.isBlank()) throw badRequest("Shipping address is required");
        String value = Normalizer.normalize(address, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
        return value.contains("hai phong") ? BigDecimal.valueOf(30000) : BigDecimal.valueOf(50000);
    }

    private String newBillCode() {
        String code;
        do {
            code = "HD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        }
        while (billRepository.existsByCode(code));
        return code;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public Bill update(int id, Bill b) {
        return billRepository.findById(id).map(x -> {
            x.setCustomerId(b.getCustomerId());
            x.setEmployeeId(b.getEmployeeId());
            x.setPtttId(b.getPtttId());
            x.setRecipientName(b.getRecipientName());
            x.setRecipientPhoneNumber(b.getRecipientPhoneNumber());
            x.setReceiverAddress(b.getReceiverAddress());
            x.setNote(b.getNote());
            return billRepository.save(x);
        }).orElseThrow(() -> badRequest("Bill not found"));
    }

    @Transactional
    public Bill transitionStatus(Integer id, Integer status, String username, String note) {
        Bill bill = billRepository.findById(id).orElseThrow(() -> badRequest("Bill not found"));
        int previous = bill.getStatus();
        if (!BillStatus.canTransition(previous, status))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid status transition: " + BillStatus.toString(previous) + " -> " + BillStatus.toString(status));
        bill.setStatus(status);
        bill.setNote(note == null ? bill.getNote() : note);
        if (status == BillStatus.HOAN_TAT) {
            bill.setStatusPayment("Đã thanh toán");
            bill.setDateOfPayment(LocalDate.now());
        }
        if (status == BillStatus.DA_HUY) restoreInventory(bill, username);
        Bill saved = billRepository.save(bill);
        saveStatusHistory(id, previous, status, username, note);
        return saved;
    }

    public List<BillStatusHistory> getStatusHistory(Integer billId) {
        return statusHistoryRepository.findByBillIdOrderByChangedAtAsc(billId);
    }

    private void restoreInventory(Bill bill, String username) {
        for (BillDetail detail : billDetailRepository.findByBillId(bill.getId())) {
            Inventory stock = inventoryRepository.lockByProductDetailId(detail.getProductDetailId())
                    .orElseThrow(() -> badRequest("Inventory not found"));
            stock.setQuantity(stock.getQuantity() + detail.getQuantity());
            saveMovement(detail.getProductDetailId(), detail.getQuantity(), stock.getQuantity(),
                    "ORDER_CANCEL", "BILL", bill.getId(), username);
        }
    }

    private void saveStatusHistory(Integer billId, Integer from, Integer to, String username, String note) {
        BillStatusHistory h = new BillStatusHistory();
        h.setBillId(billId);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setChangedBy(username);
        h.setNote(note);
        h.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(h);
    }

    private void saveMovement(Integer pdId, int change, int balance, String type, String refType, Integer refId, String user) {
        InventoryMovement m = new InventoryMovement();
        m.setProductDetailId(pdId);
        m.setQuantityChange(change);
        m.setBalanceAfter(balance);
        m.setMovementType(type);
        m.setReferenceType(refType);
        m.setReferenceId(refId);
        m.setCreatedBy(user);
        m.setCreatedAt(LocalDateTime.now());
        inventoryMovementRepository.save(m);
    }

    public Bill updateEmployee(int id, Bill b) {
        return billRepository.findById(id).map(x -> {
            x.setEmployeeId(b.getEmployeeId());
            return billRepository.save(x);
        }).orElseThrow(() -> badRequest("Bill not found"));
    }

    public void updateSubTotal(int id, BigDecimal ignored) {
        throw new ResponseStatusException(HttpStatus.GONE, "Totals are server managed");
    }

    public Bill updateStatusNote(int id, Bill b) {
        return transitionStatus(id, b.getStatus(), "legacy-admin", b.getNote());
    }

    public Bill updateStatusNote4(int id, Bill b) {
        return transitionStatus(id, b.getStatus(), "legacy-admin", b.getNote());
    }

    public Bill updateAddressByBill(int id, Bill b) {
        return billRepository.findById(id).map(x -> {
            x.setRecipientName(b.getRecipientName());
            x.setRecipientPhoneNumber(b.getRecipientPhoneNumber());
            x.setReceiverAddress(b.getReceiverAddress());
            return billRepository.save(x);
        }).orElseThrow(() -> badRequest("Bill not found"));
    }

    public Bill updateBill(int id, Bill b) {
        return billRepository.findById(id).map(x -> {
            x.setNote(b.getNote());
            return billRepository.save(x);
        }).orElseThrow(() -> badRequest("Bill not found"));
    }

    public List<InvoiceCustomerDTO> getInvoicesByCustomerId(Integer customerId) {
        List<InvoiceCustomerDTO> result = new ArrayList<>();
        for (Bill bill : billRepository.findByCustomerId(customerId)) {
            List<InvoiceItemCustomerDTO> items = billRepository.findInvoiceItemsByCustomerIdAndBillId(customerId, bill.getId());
            result.add(new InvoiceCustomerDTO(bill.getId(), bill.getCode(), bill.getRecipientName(),
                    bill.getRecipientPhoneNumber(), bill.getReceiverAddress(), bill.getCreatedDate(),
                    statusName(bill.getStatus()), bill.getGrandTotal(), items));
        }
        return result;
    }

    private String statusName(int status) {
        return switch (status) {
            case 1 -> "Chờ xác nhận";
            case 2 -> "Đã xác nhận";
            case 3 -> "Đang giao";
            case 4 -> "Hoàn thành";
            case 5 -> "Đã hủy";
            case 6 -> "Trả hàng/Hoàn tiền";
            default -> "Không xác định";
        };
    }
}
