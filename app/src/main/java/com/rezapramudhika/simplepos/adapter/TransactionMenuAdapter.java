package com.rezapramudhika.simplepos.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.activity.CashierActivity;
import com.rezapramudhika.simplepos.database.DatabaseHelper;
import com.rezapramudhika.simplepos.helper.MoneyFormat;
import com.rezapramudhika.simplepos.model.Menu;
import com.rezapramudhika.simplepos.model.Transaction;
import com.rezapramudhika.simplepos.model.TransactionMenu;

import java.util.List;

public class TransactionMenuAdapter extends RecyclerView.Adapter<TransactionMenuAdapter.ViewHolder> {

    private List<TransactionMenu> transactionMenus;
    private Activity activity;
    private DatabaseHelper db;
    private Transaction transaction;

    public TransactionMenuAdapter(List<TransactionMenu> transactionMenus, Activity activity, DatabaseHelper db, Transaction transaction) {
        this.transactionMenus = transactionMenus;
        this.activity = activity;
        this.db = db;
        this.transaction = transaction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_transaction_menu, parent, false);

        return new TransactionMenuAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final TransactionMenu transactionMenu = transactionMenus.get(position);
        final Menu menu = db.getMenu(transactionMenu.getMenuId());
        holder.menuName.setText(String.valueOf(menu.getName()));
        holder.menuPrice.setText(MoneyFormat.idr(Double.parseDouble(String.valueOf(menu.getPrice()))));
        holder.qty.setText(String.valueOf(transactionMenu.getQty()));
        holder.sumPrice.setText(MoneyFormat.idr(Double.parseDouble(String.valueOf(menu.getPrice() * Integer.parseInt(holder.qty.getText().toString().trim())))));
        if(transactionMenu.getQty() == 1){
            holder.btnMin.setEnabled(false);
        } else {
            holder.btnMin.setEnabled(true);
        }
        holder.btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.updateTransactionMenu(transaction.getId(), menu.getId(), 2);
                if(activity instanceof CashierActivity) {
                    ((CashierActivity) activity).notifyDataChange();
                }
            }
        });
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.updateTransactionMenu(transaction.getId(), menu.getId(), 1);
                if(activity instanceof CashierActivity) {
                    ((CashierActivity) activity).notifyDataChange();
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteTransactionMenu(transactionMenu.getId());
                if(activity instanceof CashierActivity) {
                    ((CashierActivity) activity).notifyDataChange();
                    ((CashierActivity) activity).setBtnCheckout();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return transactionMenus.size();
    }

    public int getTotalPrice() {
        int total = 0;
        for(int i=0; i<transactionMenus.size(); i++){
            final Menu menu = db.getMenu(transactionMenus.get(i).getMenuId());
            total += transactionMenus.get(i).getQty() * menu.getPrice();
        }
        return total;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView menuName, menuPrice, qty, sumPrice;
        public Button btnMin, btnPlus, btnDelete;

        public ViewHolder(View view) {
            super(view);
            menuName = view.findViewById(R.id.txtMenuName);
            menuPrice = view.findViewById(R.id.txtMenuPrice);
            qty = view.findViewById(R.id.txtQty);
            sumPrice = view.findViewById(R.id.txtSumPrice);
            btnMin = view.findViewById(R.id.btnMin);
            btnPlus = view.findViewById(R.id.btnPlus);
            btnDelete = view.findViewById(R.id.btnDeleteMenu);
        }
    }

}
