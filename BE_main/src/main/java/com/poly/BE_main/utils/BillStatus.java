package com.poly.BE_main.utils;

import java.util.Map;
import java.util.Set;

public final class BillStatus {
    public static final int CHO_XAC_NHAN = 1;
    public static final int DA_XAC_NHAN = 2;
    public static final int DANG_GIAO = 3;
    public static final int HOAN_TAT = 4;
    public static final int DA_HUY = 5;
    public static final int HOAN_TIEN = 6;

    private static final Map<Integer, Set<Integer>> TRANSITIONS = Map.of(
            CHO_XAC_NHAN, Set.of(DA_XAC_NHAN, DA_HUY),
            DA_XAC_NHAN, Set.of(DANG_GIAO, DA_HUY),
            DANG_GIAO, Set.of(HOAN_TAT),
            HOAN_TAT, Set.of(HOAN_TIEN),
            DA_HUY, Set.of(), HOAN_TIEN, Set.of());

    private BillStatus() {
    }

    public static boolean canTransition(int from, int to) {
        return TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public static String toString(int status) {
        return switch (status) {
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DA_XAC_NHAN -> "Đã xác nhận";
            case DANG_GIAO -> "Đang giao";
            case HOAN_TAT -> "Hoàn tất";
            case DA_HUY -> "Đã hủy";
            case HOAN_TIEN -> "Trả hàng/Hoàn tiền";
            default -> "Không xác định";
        };
    }
}
