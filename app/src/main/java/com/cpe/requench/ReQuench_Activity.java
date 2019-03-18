package com.cpe.requench;

import java.sql.Time;
import java.util.Date;

public class ReQuench_Activity {
    Date date_of_purchase;
    Time time_of_purchase;
    Double amount,price;

    public void setDate_of_purchase(Date date_of_purchase) {
        this.date_of_purchase = date_of_purchase;
    }

    public void setTime_of_purchase(Time time_of_purchase) {
        this.time_of_purchase = time_of_purchase;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getDate_of_purchase() {
        return date_of_purchase;
    }

    public Time getTime_of_purchase() {
        return time_of_purchase;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getPrice() {
        return price;
    }
}
