package com.suitexen.ecokeep.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.database.AppDatabase;
import com.suitexen.ecokeep.models.ConsumptionLog;
import com.suitexen.ecokeep.models.FoodItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InventoryInfoActivity extends AppCompatActivity {

    private ImageView ivFoodDetail;
    private TextView tvDetailName, tvDetailCategory, tvRemainingQty, tvTotalQty, tvDetailExpiry;
    private LinearProgressIndicator qtyProgress;
    private RecyclerView rvActivity;
    private ImageButton btnBack;
    private MaterialButton btnConsume, btnWaste;

    private FoodItem currentItem;
    private int foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_info);

        foodId = getIntent().getIntExtra("FOOD_ID", -1);
        if (foodId == -1) {
            finish();
            return;
        }

        initViews();
        loadData();

        btnBack.setOnClickListener(v -> finish());
        btnConsume.setOnClickListener(v -> showConsumeBottomSheet("Consumed"));
        btnWaste.setOnClickListener(v -> showConsumeBottomSheet("Wasted"));
    }

    private void initViews() {
        ivFoodDetail = findViewById(R.id.ivFoodDetail);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvRemainingQty = findViewById(R.id.tvRemainingQty);
        tvTotalQty = findViewById(R.id.tvTotalQty);
        tvDetailExpiry = findViewById(R.id.tvDetailExpiry);
        qtyProgress = findViewById(R.id.qtyProgress);
        rvActivity = findViewById(R.id.rvActivity);
        btnBack = findViewById(R.id.btnBack);
        btnConsume = findViewById(R.id.btnConsume);
        btnWaste = findViewById(R.id.btnWaste);

        rvActivity.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        currentItem = AppDatabase.getInstance(this).foodDao().getFoodItemById(foodId);
        if (currentItem == null) return;

        tvDetailName.setText(currentItem.getName());
        tvDetailCategory.setText(currentItem.getCategory());
        tvRemainingQty.setText(String.format(Locale.getDefault(), "%.1f %s", currentItem.getRemainingQuantity(), currentItem.getUnit()));
        tvTotalQty.setText(String.format(Locale.getDefault(), "%.1f %s", currentItem.getTotalQuantity(), currentItem.getUnit()));

        // Expiry Calculation
        long diff = currentItem.getExpiryDate() - System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 0) tvDetailExpiry.setText("Expired");
        else tvDetailExpiry.setText(days + " Days");

        // Progress
        int progress = (int) ((currentItem.getRemainingQuantity() / currentItem.getTotalQuantity()) * 100);
        qtyProgress.setProgress(progress);

        if (currentItem.getImageUri() != null && !currentItem.getImageUri().isEmpty()) {
            Glide.with(this).load(currentItem.getImageUri()).into(ivFoodDetail);
        }

        loadLogs();
    }

    private void loadLogs() {
        List<ConsumptionLog> logs = AppDatabase.getInstance(this).foodDao().getLogsByFoodId(foodId);
        rvActivity.setAdapter(new RecyclerView.Adapter<LogViewHolder>() {
            @NonNull
            @Override
            public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                return new LogViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
                ConsumptionLog log = logs.get(position);
                String date = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(new Date(log.getTimestamp()));
                holder.text1.setText(log.getType() + " " + log.getQuantityUsed() + " " + currentItem.getUnit());
                holder.text2.setText(date);
                
                if (log.getType().equals("Wasted")) {
                    holder.text1.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    holder.text1.setTextColor(getResources().getColor(R.color.brand_green));
                }
            }

            @Override
            public int getItemCount() {
                return logs.size();
            }
        });
    }

    private void showConsumeBottomSheet(String type) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_consume_bottom_sheet, null);
        dialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvConsumeTitle);
        TextView tvInfo = view.findViewById(R.id.tvRemainingInfo);
        TextView tvUnit = view.findViewById(R.id.tvConsumeUnit);
        EditText etQty = view.findViewById(R.id.etConsumeQty);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmConsume);

        tvTitle.setText(type + " Item");
        tvInfo.setText("Remaining: " + currentItem.getRemainingQuantity() + " " + currentItem.getUnit());
        tvUnit.setText(currentItem.getUnit());

        btnConfirm.setOnClickListener(v -> {
            String qtyStr = etQty.getText().toString().trim();
            if (qtyStr.isEmpty()) return;

            double qtyUsed = Double.parseDouble(qtyStr);
            if (qtyUsed > currentItem.getRemainingQuantity()) {
                Toast.makeText(this, "Quantity exceeds remaining amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Item
            currentItem.setConsumedQuantity(currentItem.getConsumedQuantity() + qtyUsed);
            currentItem.setRemainingQuantity(currentItem.getRemainingQuantity() - qtyUsed);
            
            if (currentItem.getRemainingQuantity() <= 0) {
                currentItem.setStatus("Finished");
            }

            AppDatabase.getInstance(this).foodDao().update(currentItem);

            // Add Log
            ConsumptionLog log = new ConsumptionLog(foodId, qtyUsed, type, System.currentTimeMillis());
            AppDatabase.getInstance(this).foodDao().insertLog(log);

            dialog.dismiss();
            loadData();
            Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        LogViewHolder(View v) {
            super(v);
            text1 = v.findViewById(android.R.id.text1);
            text2 = v.findViewById(android.R.id.text2);
        }
    }
}
