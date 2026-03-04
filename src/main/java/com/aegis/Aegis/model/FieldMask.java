package com.aegis.Aegis.model;

public class FieldMask {
    public static final int ID_MASK = 0b1;
    public static final int NAME_MASK = 0b10;
    public static final int PRICE_MASK = 0b100;
    public static final int STOCK_MASK = 0b1000;
    public static final int WAREHOUSE_MASK = 0b10000;
    public static final int LAST_UPDATED_MASK = 0b100000;
    
    public static String maskToString(int mask) {
        StringBuilder sb = new StringBuilder();
        if ((mask & ID_MASK) != 0) sb.append("ID,");
        if ((mask & NAME_MASK) != 0) sb.append("NAME,");
        if ((mask & PRICE_MASK) != 0) sb.append("PRICE,");
        if ((mask & STOCK_MASK) != 0) sb.append("STOCK,");
        if ((mask & WAREHOUSE_MASK) != 0) sb.append("WAREHOUSE,");
        if ((mask & LAST_UPDATED_MASK) != 0) sb.append("LAST_UPDATED,");
        
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }
        return sb.toString();
    }
    
    public static boolean hasFieldChanged(int mask, int fieldMask) {
        return (mask & fieldMask) != 0;
    }
}
