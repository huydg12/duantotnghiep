package com.poly.BE_main.config;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.poly.BE_main.model.Account;
import com.poly.BE_main.repository.*;

@Component("ownership")
public class OwnershipAuthorization {
    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final AddressRepository addresses;
    private final CartRepository carts;
    private final CartDetailRepository cartDetails;
    private final BillRepository bills;

    public OwnershipAuthorization(AccountRepository accounts, CustomerRepository customers,
            AddressRepository addresses, CartRepository carts, CartDetailRepository cartDetails,
            BillRepository bills) {
        this.accounts = accounts; this.customers = customers; this.addresses = addresses;
        this.carts = carts; this.cartDetails = cartDetails; this.bills = bills;
    }

    public boolean isStaff(Authentication auth) {
        return account(auth).map(a -> a.getRoleId() == 1 || a.getRoleId() == 3).orElse(false);
    }
    public boolean ownsCustomer(Authentication auth, Integer customerId) {
        if (isStaff(auth)) return true;
        return customerId != null && account(auth).flatMap(a -> customers.findByAccountId(a.getId()))
                .map(c -> c.getId().equals(customerId)).orElse(false);
    }
    public boolean ownsAddress(Authentication auth, Integer addressId) {
        return isStaff(auth) || (addressId != null && addresses.findById(addressId)
                .map(a -> ownsCustomer(auth, a.getCustomerId())).orElse(false));
    }
    public boolean ownsCart(Authentication auth, Integer cartId) {
        return isStaff(auth) || (cartId != null && carts.findById(cartId)
                .map(c -> ownsCustomer(auth, c.getCustomerId())).orElse(false));
    }
    public boolean ownsCartDetail(Authentication auth, Integer id) {
        return isStaff(auth) || (id != null && cartDetails.findById(id)
                .map(c -> ownsCart(auth, c.getCartId())).orElse(false));
    }
    public boolean ownsBill(Authentication auth, Integer billId) {
        return isStaff(auth) || (billId != null && bills.findById(billId)
                .map(b -> ownsCustomer(auth, b.getCustomerId())).orElse(false));
    }
    public boolean ownsEmployee(Authentication auth, Integer employeeId) {
        if (isStaff(auth) && account(auth).map(a -> a.getRoleId() == 1).orElse(false)) return true;
        return employeeId != null && account(auth).flatMap(a -> java.util.Optional.ofNullable(a.getEmployee()))
                .map(e -> e.getId().equals(employeeId)).orElse(false);
    }
    private java.util.Optional<Account> account(Authentication auth) {
        return auth == null ? java.util.Optional.empty() : accounts.findByUsername(auth.getName());
    }
}
