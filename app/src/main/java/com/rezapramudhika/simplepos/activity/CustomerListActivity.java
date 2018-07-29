package com.rezapramudhika.simplepos.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.adapter.CustomerAdapter;
import com.rezapramudhika.simplepos.cache.Memcache;
import com.rezapramudhika.simplepos.database.DatabaseHelper;
import com.rezapramudhika.simplepos.helper.MoneyFormat;
import com.rezapramudhika.simplepos.model.Menu;
import com.rezapramudhika.simplepos.model.Transaction;
import com.rezapramudhika.simplepos.model.TransactionBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomerListActivity extends AppCompatActivity {

    private TextView txtNoCustomer;
    private RecyclerView recyclerViewCustomerList;
    private Button btnOpenDialogCustomer, btnAddNewCustomer;
    private EditText inputTableNumber;
    private List<Transaction> transactions = new ArrayList<>();
    private CustomerAdapter mAdapter;
    private DatabaseHelper db;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        Toolbar toolbar = findViewById(R.id.toolbarCustomerList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Daftar Pelanggan");

        db = new DatabaseHelper(this);

        txtNoCustomer = findViewById(R.id.txtNoCustomer);
        btnOpenDialogCustomer = findViewById(R.id.btnOpenDialogCustomer);
        recyclerViewCustomerList = findViewById(R.id.recyclerViewCustomerList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                notifyDatasetChange();
            }
        });
        prepareCustomerList();

        btnOpenDialogCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CustomerListActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
                btnAddNewCustomer = mView.findViewById(R.id.btnAddNewCustomer);
                inputTableNumber = mView.findViewById(R.id.inputTableNumber);
                btnAddNewCustomer.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
                btnAddNewCustomer.setEnabled(false);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                inputTableNumber.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(s.length() != 0) {
                            if(transactions.size() != 0){
                                for(int i=0; i<transactions.size(); i++) {
                                    if(transactions.get(i).getTableNumber() == Integer.parseInt(String.valueOf(s))) {
                                        inputTableNumber.setError("Meja sudah digunakan");
                                        btnAddNewCustomer.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
                                        btnAddNewCustomer.setEnabled(false);
                                    }else{
                                        btnAddNewCustomer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                        btnAddNewCustomer.setEnabled(true);
                                    }
                                }
                            } else {
                                btnAddNewCustomer.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                btnAddNewCustomer.setEnabled(true);
                            }
                        } else {
                            btnAddNewCustomer.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
                            btnAddNewCustomer.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                btnAddNewCustomer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.insertTransaction(CustomerListActivity.this, Integer.parseInt(inputTableNumber.getText().toString()));
                        prepareCustomerList();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void notifyDatasetChange() {
        transactions.clear();
        transactions = db.getAllTransaction();
        if(transactions.size() == 0) {
            txtNoCustomer.setVisibility(View.VISIBLE);
            recyclerViewCustomerList.setVisibility(View.GONE);
        } else {
            txtNoCustomer.setVisibility(View.GONE);
            recyclerViewCustomerList.setVisibility(View.VISIBLE);
        }
        mAdapter = new CustomerAdapter(transactions, getApplicationContext());
        recyclerViewCustomerList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void prepareCustomerList() {
        transactions.clear();
        transactions = db.getAllTransaction();
        if(transactions.size() == 0) {
            txtNoCustomer.setVisibility(View.VISIBLE);
            recyclerViewCustomerList.setVisibility(View.GONE);
        } else {
            txtNoCustomer.setVisibility(View.GONE);
            recyclerViewCustomerList.setVisibility(View.VISIBLE);
        }
        mAdapter = new CustomerAdapter(transactions, getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewCustomerList.setLayoutManager(mLayoutManager);
        recyclerViewCustomerList.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCustomerList.setAdapter(mAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void logout() {
        Memcache memcache = new Memcache(getApplicationContext());
        memcache.logout();
        startActivity(new Intent(CustomerListActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyDatasetChange();
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CustomerListActivity.this);
                alertDialogBuilder.setTitle("Logout");
                alertDialogBuilder.setMessage("Apakah anda yakin?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        logout();
                    }
                });
                alertDialogBuilder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        logout();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
