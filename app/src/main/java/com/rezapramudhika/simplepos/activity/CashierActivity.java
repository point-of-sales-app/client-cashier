package com.rezapramudhika.simplepos.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.adapter.CustomerAdapter;
import com.rezapramudhika.simplepos.adapter.MenuAdapter;
import com.rezapramudhika.simplepos.adapter.TransactionMenuAdapter;
import com.rezapramudhika.simplepos.cache.Memcache;
import com.rezapramudhika.simplepos.database.DatabaseHelper;
import com.rezapramudhika.simplepos.helper.MoneyFormat;
import com.rezapramudhika.simplepos.model.Category;
import com.rezapramudhika.simplepos.model.LoginBody;
import com.rezapramudhika.simplepos.model.LoginResponse;
import com.rezapramudhika.simplepos.model.Menu;
import com.rezapramudhika.simplepos.model.Transaction;
import com.rezapramudhika.simplepos.model.TransactionBody;
import com.rezapramudhika.simplepos.model.TransactionMenu;
import com.rezapramudhika.simplepos.model.TransactionResponse;
import com.rezapramudhika.simplepos.rest.ApiClient;
import com.rezapramudhika.simplepos.rest.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CashierActivity extends AppCompatActivity {
    private static final String TAG = CashierActivity.class.getSimpleName();
    private DatabaseHelper db;
    private Spinner spnCategory;
    private List<Category> categories;
    private ArrayAdapter<String> categoryAdapter;
    private GridView menuGridView;
    private MenuAdapter menuAdapter;
    private Transaction transaction = new Transaction();
    private List<TransactionMenu> transactionMenus = new ArrayList<>();
    private TextView tableNumber, cashierName, datetime, grandTotal;
    private TransactionMenuAdapter mAdapter;
    private RecyclerView recyclerViewTransactionMenu;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Menu> menus;
    private Button btnCheckout, btnSave;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Mohon tunggu sebentar...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        prepareTransaction();

        Toolbar toolbar = findViewById(R.id.toolbarCashier);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Meja Nomor "+transaction.getTableNumber());

        db = new DatabaseHelper(this);

        spnCategory = findViewById(R.id.spinnerCashierCategory);

        categories = db.getAllCategories();
        Log.d("CATEGORIES LENGHT", String.valueOf(categories.size()));
        final String[] categoryNames = new String[categories.size()+1];
        categoryNames[0] = "Semua menu";

        int key = 1;
        for(Category category : categories) {
            categoryNames[key] = category.getName();
            key++;
        }
        categoryAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spnCategory.setAdapter(categoryAdapter);

        spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0) {
                    menus = db.getMenus(categories.get(i-1).getId());
                    getMenu(menus);
                    menuGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            db.insertTransactionMenu(transaction.getId(), menus.get(position).getId());
                            prepareNota();
                        }
                    });
                } else {
                    menus = db.getMenus(0);
                    getMenu(menus);
                    menuGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            db.insertTransactionMenu(transaction.getId(), menus.get(position).getId());
                            prepareNota();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recyclerViewTransactionMenu = findViewById(R.id.recyclerViewTransactionMenu);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                notifyDataChange();
            }
        });

        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.updateTransaction(transaction.getId(), getTotalPrice());
                finish();
            }
        });

        btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputTableNumber, inputTotal, inputCash;
                final Button btnPay;
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(CashierActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_checkout, null);
                inputTableNumber = mView.findViewById(R.id.inputTableNumber);
                inputTotal = mView.findViewById(R.id.inputTotal);
                inputCash = mView.findViewById(R.id.inputCash);
                btnPay = mView.findViewById(R.id.btnPay);

                inputTableNumber.setFocusable(false);
                inputTotal.setFocusable(false);
                btnPay.setEnabled(false);

                inputTableNumber.setText(String.valueOf(transaction.getTableNumber()));
                inputTotal.setText(MoneyFormat.idr(Double.valueOf(String.valueOf(getTotalPrice()))));

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                inputCash.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(s.length() != 0) {
                            if(Integer.parseInt(inputCash.getText().toString()) < getTotalPrice()) {
                                inputCash.setError("Jumlah uang kurang");
                                btnPay.setEnabled(false);
                            } else {
                                btnPay.setEnabled(true);
                            }
                        } else {
                            btnPay.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                btnPay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Integer.parseInt(inputCash.getText().toString()) > getTotalPrice()) {
                            dialog.dismiss();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CashierActivity.this);
                            alertDialogBuilder.setTitle("Kembalian");
                            alertDialogBuilder.setMessage(
                                    MoneyFormat.idr(
                                            Double.valueOf(String.valueOf(
                                                    Integer.parseInt(inputCash.getText().toString()) - getTotalPrice())
                                            )
                                    )
                            );
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    transactionMenus = db.getTransactionMenu(transaction.getId());
                                    TransactionBody transactionBody = new TransactionBody(
                                            getTotalPrice(),
                                            1,
                                            new Memcache(getApplicationContext()).getUser().getId(),
                                            transactionMenus);
                                    postTransaction(
                                            new Memcache(getApplicationContext()).getUser().getToken(),
                                            String.valueOf(new Memcache(getApplicationContext()).getRestaurant().getId()),
                                            transactionBody
                                    );
                                    db.deleteTransaction(transaction.getId());
                                    finish();
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        } else {
                            transactionMenus = db.getTransactionMenu(transaction.getId());
                            TransactionBody transactionBody = new TransactionBody(
                                    getTotalPrice(),
                                    1,
                                    new Memcache(getApplicationContext()).getUser().getId(),
                                    transactionMenus);
                            postTransaction(
                                    new Memcache(getApplicationContext()).getUser().getToken(),
                                    String.valueOf(new Memcache(getApplicationContext()).getRestaurant().getId()),
                                    transactionBody
                            );
                        }
                    }
                });

            }
        });

        prepareData();
        prepareNota();
        setBtnCheckout();
    }

    private void postTransaction (String token, String restaurantId, TransactionBody transactionBody) {
        mProgressDialog.show();
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<TransactionResponse> call = apiService.postTransaction(token, restaurantId, transactionBody);
        call.enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse>call, Response<TransactionResponse> response) {
                mProgressDialog.dismiss();
                db.deleteTransaction(transaction.getId());
                finish();
            }

            @Override
            public void onFailure(Call<TransactionResponse>call, Throwable t) {
                // Log error here since request failed
                mProgressDialog.dismiss();
                Log.e(TAG, t.toString());
            }
        });
    }

    public void setBtnCheckout() {
        if ( transactionMenus.size() == 0 ){
            btnCheckout.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
            btnCheckout.setEnabled(false);
        } else {
            btnCheckout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            btnCheckout.setEnabled(true);
        }
    }

    public void notifyDataChange() {
        transactionMenus.clear();
        transactionMenus = db.getTransactionMenu(transaction.getId());
        mAdapter = new TransactionMenuAdapter(transactionMenus, this, db, transaction);
        recyclerViewTransactionMenu.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        setTotalPrice();
    }

    private void getMenu(List<Menu> menus) {
        menuGridView = findViewById(R.id.gridMenu);
        menuAdapter = new MenuAdapter(getApplicationContext(), R.layout.list_item_menu, menus);
        menuGridView.setAdapter(menuAdapter);
    }

    private Transaction prepareTransaction() {
        Bundle bundle = getIntent().getExtras();
        int id = bundle.getInt("id");
        int paymentMethodId = bundle.getInt("paymentMethodId");
        int restaurantId = bundle.getInt("restaurantId");
        int tableNumber = bundle.getInt("tableNumber");
        int total = bundle.getInt("total");
        int userId = bundle.getInt("userId");
        transaction.setUserId(userId);
        transaction.setTableNumber(tableNumber);
        transaction.setTotal(total);
        transaction.setRestaurantId(restaurantId);
        transaction.setPaymentMethodId(paymentMethodId);
        transaction.setId(id);

        return transaction;
    }

    private void prepareData() {
        tableNumber = findViewById(R.id.txtTableNumber);
        cashierName = findViewById(R.id.txtCashierName);
        datetime = findViewById(R.id.txtDateTime);

        tableNumber.setText("No. Meja: "+String.valueOf(transaction.getTableNumber()));
        cashierName.setText("Cashier: "+new Memcache(getApplicationContext()).getUser().getName());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(calendar.getTime());

        datetime.setText(formattedDate);
    }

    public void prepareNota() {
        transactionMenus.clear();
        transactionMenus = db.getTransactionMenu(transaction.getId());

        mAdapter = new TransactionMenuAdapter(transactionMenus, this, db, transaction);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewTransactionMenu.setLayoutManager(mLayoutManager);
        recyclerViewTransactionMenu.setItemAnimator(new DefaultItemAnimator());
        recyclerViewTransactionMenu.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        recyclerViewTransactionMenu.setAdapter(mAdapter);
        swipeRefreshLayout.setRefreshing(false);
        setTotalPrice();
        setBtnCheckout();
    }

    private void setTotalPrice() {
        int total = 0;
        if(transactionMenus.size() >= 1) {
            for(int i=0; i<transactionMenus.size(); i++){
                final Menu menu = db.getMenu(transactionMenus.get(i).getMenuId());
                total += transactionMenus.get(i).getQty() * menu.getPrice();
            }
        }
        grandTotal = findViewById(R.id.txtGrandTotal);
        grandTotal.setText("Total: "+ MoneyFormat.idr(Double.valueOf(String.valueOf(total))));
    }

    private int getTotalPrice() {
        int total = 0;
        if(transactionMenus.size() >= 1) {
            for(int i=0; i<transactionMenus.size(); i++){
                final Menu menu = db.getMenu(transactionMenus.get(i).getMenuId());
                total += transactionMenus.get(i).getQty() * menu.getPrice();
            }
        }
        return total;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.updateTransaction(transaction.getId(), getTotalPrice());
    }
}
