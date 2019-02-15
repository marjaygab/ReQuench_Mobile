package com.cpe.requench;

import java.sql.Time;
import java.util.Date;

public class Transaction_History {
    int Transaction_ID;
    String Machine_Loc;
    Date date_of_purchase;
    Time time_of_purchase;
    String Temperature;
    Double amount,price,remaining_balance;

    public Transaction_History(int transaction_ID, String machine_loc, Date date_of_purchase,Time top,String temp, Double amount, Double price, Double remaining_balance) {
        this.Transaction_ID = transaction_ID;
        this.Machine_Loc = machine_loc;
        this.date_of_purchase = date_of_purchase;
        this.time_of_purchase = top;
        this.amount = amount;
        this.price = price;
        this.Temperature = temp;
        this.remaining_balance = remaining_balance;
    }

}
