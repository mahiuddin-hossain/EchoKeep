package com.suitexen.ecokeep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.models.FoodItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<FoodItem> foodItemList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public InventoryAdapter(List<FoodItem> foodItemList) {
        this.foodItemList = foodItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodItemList.get(position);
        holder.tvItemName.setText(item.getName());
        holder.tvWeight.setText(String.format(Locale.getDefault(), "%.1f %s", item.getQuantity(), item.getUnit()));
        holder.tvExpiry.setText("Expires: " + dateFormat.format(item.getExpiryDate()));
        
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUri())
                    .placeholder(R.drawable.ic_inventory)
                    .into(holder.ivItemImage);
        } else {
            // Default images based on category
            if (item.getCategory().equalsIgnoreCase("Vegetables")) {
                holder.ivItemImage.setImageResource(R.drawable.img_veg2);
            } else if (item.getCategory().equalsIgnoreCase("Fruits")) {
                holder.ivItemImage.setImageResource(R.drawable.img_veg1);
            } else {
                holder.ivItemImage.setImageResource(R.drawable.ic_inventory);
            }
        }
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvItemName, tvWeight, tvExpiry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
        }
    }
}
