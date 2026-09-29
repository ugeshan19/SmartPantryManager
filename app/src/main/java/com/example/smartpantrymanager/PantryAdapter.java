package com.example.smartpantrymanager;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter responsible for displaying pantry items inside the RecyclerView.
 * Each row has an Edit and a Delete button; the adapter reports those
 * clicks to the hosting Activity through OnItemActionListener.
 * When expiry alerts are enabled, expired and expiring-soon items are
 * highlighted in red / orange.
 */
public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Callback so MainActivity decides what Edit and Delete actually do. */
    public interface OnItemActionListener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    // List containing all pantry items that need to be displayed.
    private final List<PantryItem> pantryItems;

    // True when the user has turned on expiring-soon alerts in Settings.
    private final boolean alertsEnabled;

    // Listener that receives Edit / Delete clicks.
    private final OnItemActionListener listener;

    public PantryAdapter(List<PantryItem> pantryItems,
                         boolean alertsEnabled,
                         OnItemActionListener listener) {
        this.pantryItems = pantryItems;
        this.alertsEnabled = alertsEnabled;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        // Inflate the custom pantry item layout.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder,
            int position) {

        // Get the pantry item for the current RecyclerView position.
        PantryItem item = pantryItems.get(position);

        holder.tvIngredientName.setText(item.getName());

        holder.tvIngredientDetails.setText(
                item.getQuantityText() + " " + item.getUnit()
        );

        bindExpiry(holder, item);

        // Forward the button clicks to the Activity.
        holder.btnEditItem.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDeleteItem.setOnClickListener(v -> listener.onDelete(item));
    }

    /**
     * Shows the expiry text. RecyclerView re-uses row views, so the colour
     * and style are ALWAYS reset first, otherwise a red row could be
     * recycled into a normal one.
     */
    private void bindExpiry(PantryViewHolder holder, PantryItem item) {

        // Reset to the normal look.
        holder.tvExpiryDate.setTextColor(holder.defaultExpiryColor);
        holder.tvExpiryDate.setTypeface(null, Typeface.NORMAL);

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.tvExpiryDate.setText("No expiry date");
            return;
        }

        String text = "Expires: " + expiry;

        if (alertsEnabled && item.isExpiringWithin(AppSettings.EXPIRY_ALERT_DAYS)) {
            long days = item.getDaysUntilExpiry();
            int color;

            if (days < 0) {
                text = "EXPIRED: " + expiry;
                color = R.color.expired_red;
            } else if (days == 0) {
                text = "Expires TODAY: " + expiry;
                color = R.color.expiring_orange;
            } else if (days == 1) {
                text = "Expires tomorrow: " + expiry;
                color = R.color.expiring_orange;
            } else {
                text = "Expires in " + days + " days: " + expiry;
                color = R.color.expiring_orange;
            }

            holder.tvExpiryDate.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), color));
            holder.tvExpiryDate.setTypeface(null, Typeface.BOLD);
        }

        holder.tvExpiryDate.setText(text);
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    /**
     * ViewHolder stores references to the views used
     * for displaying one pantry item.
     */
    public static class PantryViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvIngredientName;
        TextView tvIngredientDetails;
        TextView tvExpiryDate;
        Button btnEditItem;
        Button btnDeleteItem;

        // The normal text colour, remembered so it can be restored.
        final int defaultExpiryColor;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvIngredientDetails = itemView.findViewById(R.id.tvIngredientDetails);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            btnEditItem = itemView.findViewById(R.id.btnEditItem);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);

            defaultExpiryColor = tvExpiryDate.getCurrentTextColor();
        }
    }
}