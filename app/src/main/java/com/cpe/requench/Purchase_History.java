package com.cpe.requench;

import java.sql.Time;
import java.util.Date;

public class Purchase_History extends ReQuench_Activity{
    int Purchase_ID;

    public Purchase_History(Date date_of_purchase, Time time_of_purchase, Double amount, Double price, int purchase_ID) {
        setAmount(amount);
        setDate_of_purchase(date_of_purchase);
        setPrice(price);
        setTime_of_purchase(time_of_purchase);
        Purchase_ID = purchase_ID;
    }
}
