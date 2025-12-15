package com.example.financemanager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financemanager.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ClickEvent {
    ActivityMainBinding binding;

    FinanceAdapter financeAdapter;
    FinanceDatabase financeDatabase;
    FinanceDao financeDao;

    private static final int SMS_PERMISSION_CODE = 100;

    long expense = 0, income = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ NEW: Notification Channel Setup
        NotificationHelper.createNotificationChannel(this);

        // SMS Permission Check Logic
        checkSmsPermission();

        binding.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, addActivity.class));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted. Auto-tracking enabled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied. Auto-tracking will not work.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        financeDatabase = FinanceDatabase.getInstance(this);
        financeDao = financeDatabase.getDao();

        updateData(financeDao.getAll());
        initRecycler(financeDao.getAll());
    }

    private void updateData(List<FinanceTable> financeTableList) {
        expense = 0;
        income = 0;

        for (FinanceTable financeTable : financeTableList) {
            if (financeTable.isIncome()) {
                income += financeTable.getAmount();
            } else {
                expense += financeTable.getAmount();
            }
        }

        binding.totalBalance.setText(String.valueOf(income - expense));
        binding.totalIncome.setText(String.valueOf(income));
        binding.totalExpense.setText(String.valueOf(expense));
    }

    private void initRecycler(List<FinanceTable> financeTableList) {
        financeAdapter = new FinanceAdapter(this, this);
        binding.itemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.itemsRecycler.setAdapter(financeAdapter);

        for (FinanceTable financeTable : financeTableList) {
            financeAdapter.add(financeTable);
        }
    }

    @Override
    public void OnClick(int pos) {
        Intent intent = new Intent(MainActivity.this, addActivity.class);
        intent.putExtra("update", true);
        intent.putExtra("id", financeAdapter.getId(pos));

        intent.putExtra("title", financeAdapter.title(pos));
        intent.putExtra("category", financeAdapter.category(pos));

        intent.putExtra("amount", financeAdapter.amount(pos));
        intent.putExtra("isIncome", financeAdapter.isIncome(pos));
        intent.putExtra("date", financeAdapter.date(pos));
        intent.putExtra("description", financeAdapter.desc(pos));

        startActivity(intent);
    }

    @Override
    public void OnLongPress(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete")
                .setMessage("Do you want to delete it")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int id = financeAdapter.getId(pos);
                        financeDao.delete(id);
                        financeAdapter.delete(pos);
                        updateData(financeDao.getAll());
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        builder.show();
    }
}