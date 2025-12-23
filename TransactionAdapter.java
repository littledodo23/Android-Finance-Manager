package com.finance.manager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    
    private List<Transaction> transactions;
    private OnTransactionClickListener listener;
    
    public interface OnTransactionClickListener {
        void onEditClick(Transaction transaction);
        void onDeleteClick(Transaction transaction);
    }
    
    public TransactionAdapter(List<Transaction> transactions, OnTransactionClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        
        holder.categoryText.setText(transaction.getCategory());
        holder.amountText.setText(transaction.getFormattedAmount());
        holder.dateText.setText(transaction.getFormattedDate());
        holder.descriptionText.setText(transaction.getDescription());
        
        // Set color based on type
        if ("income".equals(transaction.getType())) {
            holder.amountText.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.amountText.setTextColor(Color.parseColor("#F44336")); // Red
        }
        
        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(transaction);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(transaction);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return transactions.size();
    }
    
    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }
    
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView categoryText, amountText, dateText, descriptionText;
        ImageButton editButton, deleteButton;
        
        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.categoryText);
            amountText = itemView.findViewById(R.id.amountText);
            dateText = itemView.findViewById(R.id.dateText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
