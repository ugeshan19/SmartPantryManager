package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter responsible for displaying pantry items inside the RecyclerView.
 * Each row has an Edit and a Delete button; the adapter reports those
 * clicks to the hosting Activity through OnItemActionListener.
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

    // Listener that receives Edit / Delete clicks.
    private final OnItemActionListener listener;

    public PantryAdapter(List<PantryItem> pantryItems,
                         OnItemActionListener listener) {
        this.pantryItems = pantryItems;
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

        // Show the expiry date if one was provided.
        if (item.getExpiryDate() != null
                && !item.getExpiryDate().isEmpty()) {
            holder.tvExpiryDate.setText("Expires: " + item.getExpiryDate());
        } else {
            holder.tvExpiryDate.setText("No expiry date");
        }

        // Forward the button clicks to the Activity.
        holder.btnEditItem.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDeleteItem.setOnClickListener(v -> listener.onDelete(item));
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

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvIngredientDetails = itemView.findViewById(R.id.tvIngredientDetails);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);
            btnEditItem = itemView.findViewById(R.id.btnEditItem);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}