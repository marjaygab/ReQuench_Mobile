package com.cpe.requench;

import java.sql.Time;
import java.util.Date;

public class Transaction_History extends ReQuench_Activity {
    int Transaction_ID;
    String Machine_Loc;
    String Temperature;
    Double remaining_balance;

    public Transaction_History(Date date_of_purchase, Time time_of_purchase, Double amount, Double price, int transaction_ID, String machine_Loc, String temperature, Double remaining_balance) {
        setAmount(amount);
        setDate_of_purchase(date_of_purchase);
        setPrice(price);
        setTime_of_purchase(time_of_purchase);
        this.Transaction_ID = transaction_ID;
        this.Machine_Loc = machine_Loc;
        this.Temperature = temperature;
        this.remaining_balance = remaining_balance;
    }
}
