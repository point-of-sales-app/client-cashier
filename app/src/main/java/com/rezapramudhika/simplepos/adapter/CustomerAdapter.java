package com.rezapramudhika.simplepos.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.activity.CashierActivity;
import com.rezapramudhika.simplepos.helper.MoneyFormat;
import com.rezapramudhika.simplepos.model.Transaction;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder>{

    private List<Transaction> transactions;
    private Context context;

    public CustomerAdapter(List<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_customer, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Transaction transaction = transactions.get(position);
        holder.tableNumber.setText("Meja Nomor "+String.valueOf(transaction.getTableNumber()));
        holder.total.setText("Total Transaksi: "+ MoneyFormat.idr(Double.valueOf(String.valueOf(transaction.getTotal()))));
        holder.listItemCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("id", transaction.getId());
                bundle.putInt("paymentMethodId", transaction.getPaymentMethodId());
                bundle.putInt("restaurantId", transaction.getRestaurantId());
                bundle.putInt("tableNumber", transaction.getTableNumber());
                bundle.putInt("total", transaction.getTotal());
                bundle.putInt("userId", transaction.getUserId());

                Intent intent = new Intent(context, CashierActivity.class).putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tableNumber, total;
        public LinearLayout listItemCustomer;

        public ViewHolder(View view) {
            super(view);
            tableNumber = view.findViewById(R.id.txtTableNumber);
            total = view.findViewById(R.id.txtTotal);
            listItemCustomer = view.findViewById(R.id.listItemCustomer);
        }
    }
}
