package com.suitexen.ecokeep.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.database.AppDatabase;
import com.suitexen.ecokeep.models.ConsumptionLog;
import com.suitexen.ecokeep.models.FoodItem;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvInventoryCount, tvSavingsAmount, tvItemCount;
    private CardView cardExpiring, cardScanReceipt, cardAddItem, cardFindRecipe;
    private ImageView ivNotification;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvInventoryCount = view.findViewById(R.id.tvInventoryCount);
        tvSavingsAmount = view.findViewById(R.id.tvSavingsAmount);
        tvItemCount = view.findViewById(R.id.tvItemCount);
        cardExpiring = view.findViewById(R.id.cardExpiring);
        cardScanReceipt = view.findViewById(R.id.cardScanReceipt);
        cardAddItem = view.findViewById(R.id.cardAddItem);
        cardFindRecipe = view.findViewById(R.id.cardFindRecipe);
        ivNotification = view.findViewById(R.id.ivNotification);

        cardScanReceipt.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.suitexen.ecokeep.ui.activities.ScanReceiptActivity.class);
            startActivity(intent);
        });

        loadHomeStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHomeStats();
    }

    private void loadHomeStats() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        
        // 1. Get Inventory Count
        List<FoodItem> activeItems = db.foodDao().getAllFoodItems();
        tvInventoryCount.setText(String.valueOf(activeItems.size()));

        // 2. Get Savings Amount (Calculated from Consumed Logs)
        // Note: For real savings, we need to join FoodItem with logs or calculate from items.
        // For now, let's sum up prices of all items that had consumption logs.
        double totalSavings = 0;
        List<FoodItem> allItems = db.foodDao().getAllFoodItems(); // Ideally would include Finished too
        // Since getAllFoodItems currently filters out Finished, we might need a better query later.
        // But let's calculate based on current items for now.
        
        for (FoodItem item : allItems) {
            if (item.getConsumedQuantity() > 0) {
                // Pro-rata savings calculation: (Consumed / Total) * Price
                double itemSavings = (item.getConsumedQuantity() / item.getTotalQuantity()) * item.getPrice();
                totalSavings += itemSavings;
            }
        }
        tvSavingsAmount.setText(String.format(Locale.getDefault(), "$%.2f", totalSavings));

        // 3. Expiring Soon Count (items expiring in next 3 days)
        int expiringCount = 0;
        long threeDaysInMs = 3 * 24 * 60 * 60 * 1000L;
        long currentTime = System.currentTimeMillis();
        
        for (FoodItem item : activeItems) {
            if (item.getExpiryDate() - currentTime <= threeDaysInMs && item.getExpiryDate() > currentTime) {
                expiringCount++;
            }
        }
        tvItemCount.setText(expiringCount + " items");
        
        if (expiringCount > 0) {
            cardExpiring.setVisibility(View.VISIBLE);
        } else {
            cardExpiring.setVisibility(View.GONE);
        }
    }
}
