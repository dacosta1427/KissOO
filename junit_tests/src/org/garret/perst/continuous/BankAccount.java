package org.garret.perst.continuous;

import org.garret.perst.*;

class BankAccount extends CVersion
{ 
    @Indexable
    int id;

    long balance;

    BankAccount() {}
}

class BankTransfer extends CVersion
{  
    CVersionHistory<BankAccount> src;
    CVersionHistory<BankAccount> dst;
    long amount;

    BankTransfer(BankAccount src, BankAccount dst, long amount) 
    { 
        this.src = src.getVersionHistory();
        this.dst = dst.getVersionHistory();
        this.amount = amount;
    }

    BankTransfer() {}
}
