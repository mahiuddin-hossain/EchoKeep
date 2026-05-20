package com.suitexen.ecokeep.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.models.ScannedItem;

import java.util.List;

public class ScannedItemAdapter extends RecyclerView.Adapter<ScannedItemAdapter.ViewHolder> {

    public interface OnEditClickListener {
        void onEditClick(int position, ScannedItem item);
    }

    private final List<ScannedItem> items;
    private OnEditClickListener editClickListener;

    public ScannedItemAdapter(List<ScannedItem> items) {
        this.items = items;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedItem item = items.get(position);

        holder.tvFoodEmoji.setText(item.getEmoji());
        holder.tvItemName.setText(item.getName());
        holder.tvItemQuantity.setText(item.getQuantity());

        holder.divider.setVisibility(
                position < items.size() - 1 ? View.VISIBLE : View.GONE
        );

        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(holder.getAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItem(int position, ScannedItem updatedItem) {
        if (position >= 0 && position < items.size()) {
            items.set(position, updatedItem);
            notifyItemChanged(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodEmoji, tvItemName, tvItemQuantity;
        ImageButton btnEdit;
        View divider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodEmoji     = itemView.findViewById(R.id.tvFoodEmoji);
            tvItemName      = itemView.findViewById(R.id.tvItemName);
            tvItemQuantity  = itemView.findViewById(R.id.tvItemQuantity);
            btnEdit         = itemView.findViewById(R.id.btnEdit);
            divider         = itemView.findViewById(R.id.divider);
        }
    }
}