package com.example.financemanager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FinanceDao {

    @Insert
    void insertFinance(FinanceTable financeTable);

    @Update
    void updateFinance(FinanceTable financeTable);

    @Query("DELETE FROM finance WHERE id = :id")
    void delete(int id);

    @Query("select * from finance")
    List<FinanceTable> getAll();
}