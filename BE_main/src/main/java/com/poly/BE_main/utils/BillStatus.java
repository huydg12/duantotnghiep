package com.poly.BE_main.utils;

public class BillStatus {
    public static final int CHO_XAC_NHAN = 1;
    public static final int DA_XAC_NHAN = 2;
    public static final int DANG_GIAO = 3;
    public static final int HOAN_TAT = 4;
    public static final int DA_HUY = 5;

    public static String toString(int status) {
        switch (status) {
            case CHO_XAC_NHAN:
                return "Chờ xác nhận";
            case DA_XAC_NHAN:
                return "Đã xác nhận";
            case DANG_GIAO:
                return "Đang giao";
            case HOAN_TAT:
                return "Hoàn tất";
            case DA_HUY:
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }
}
