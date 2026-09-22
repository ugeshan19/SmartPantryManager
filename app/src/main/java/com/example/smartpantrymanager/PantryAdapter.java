package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter responsible for displaying pantry items inside the RecyclerView.
 *
 * The adapter receives pantry data from MainActivity and binds each
 * PantryItem object to the item_pantry.xml layout.
 */
public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    // List containing all pantry items that need to be displayed
    private List<PantryItem> pantryItems;

    /**
     * Constructor receives the pantry item list from MainActivity.
     */
    public PantryAdapter(List<PantryItem> pantryItems) {
        this.pantryItems = pantryItems;
    }

    /**
     * Creates a new ViewHolder when the RecyclerView needs a new
     * list item to display.
     */
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        // Inflate the custom pantry item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        // Return a ViewHolder containing the inflated layout
        return new PantryViewHolder(view);
    }

    /**
     * Connects the PantryItem data to the views displayed
     * inside each RecyclerView item.
     */
    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder,
            int position) {

        // Get the pantry item for the current RecyclerView position
        PantryItem item = pantryItems.get(position);

        // Display the ingredient name
        holder.tvIngredientName.setText(item.getName());

        // Display the quantity and unit
        holder.tvIngredientDetails.setText(
                item.getQuantity() + " " + item.getUnit()
        );

        // Display the expiry date if one was provided
        if (item.getExpiryDate() != null
                && !item.getExpiryDate().isEmpty()) {

            holder.tvExpiryDate.setText(
                    "Expires: " + item.getExpiryDate()
            );

        } else {

            // Display this message when no expiry date was entered
            holder.tvExpiryDate.setText("No expiry date");
        }
    }

    /**
     * Returns the number of pantry items currently in the list.
     */
    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    /**
     * ViewHolder stores references to the TextViews used
     * for displaying one pantry item.
     */
    public static class PantryViewHolder
            extends RecyclerView.ViewHolder {

        // TextView displaying the ingredient name
        TextView tvIngredientName;

        // TextView displaying quantity and unit
        TextView tvIngredientDetails;

        // TextView displaying the expiry date
        TextView tvExpiryDate;

        /**
         * Constructor connects the Java variables to the
         * views defined in item_pantry.xml.
         */
        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvIngredientName =
                    itemView.findViewById(R.id.tvIngredientName);

            tvIngredientDetails =
                    itemView.findViewById(R.id.tvIngredientDetails);

            tvExpiryDate =
                    itemView.findViewById(R.id.tvExpiryDate);
        }
    }
}