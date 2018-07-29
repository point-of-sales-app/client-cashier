package com.rezapramudhika.simplepos.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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

import com.mocoo.hang.rtprinter.driver.Contants;
import com.mocoo.hang.rtprinter.driver.HsBluetoothPrintDriver;
import com.rezapramudhika.simplepos.R;
import com.rezapramudhika.simplepos.adapter.MenuAdapter;
import com.rezapramudhika.simplepos.adapter.TransactionMenuAdapter;
import com.rezapramudhika.simplepos.cache.Memcache;
import com.rezapramudhika.simplepos.database.DatabaseHelper;
import com.rezapramudhika.simplepos.helper.MoneyFormat;
import com.rezapramudhika.simplepos.helper.StaticValue;
import com.rezapramudhika.simplepos.model.Category;
import com.rezapramudhika.simplepos.model.GetLastIdResponse;
import com.rezapramudhika.simplepos.model.Menu;
import com.rezapramudhika.simplepos.model.Restaurant;
import com.rezapramudhika.simplepos.model.Transaction;
import com.rezapramudhika.simplepos.model.TransactionBody;
import com.rezapramudhika.simplepos.model.TransactionMenu;
import com.rezapramudhika.simplepos.model.TransactionResponse;
import com.rezapramudhika.simplepos.model.User;
import com.rezapramudhika.simplepos.rest.ApiClient;
import com.rezapramudhika.simplepos.rest.ApiInterface;

import java.lang.ref.WeakReference;
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

    private static BluetoothDevice device;
    private static Context CONTEXT;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter = null;
    public static HsBluetoothPrintDriver BLUETOOTH_PRINTER = null;

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashier);

        CONTEXT = getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not available in your device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }

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
                try {
                    getLastId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                btnPay.setBackgroundColor(getResources().getColor(R.color.btnDisabled));

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
                                btnPay.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
                                btnPay.setEnabled(false);
                            } else {
                                btnPay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                btnPay.setEnabled(true);
                            }
                        } else {
                            btnPay.setBackgroundColor(getResources().getColor(R.color.btnDisabled));
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
                        if (mBluetoothAdapter == null) {
                            dialog.dismiss();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CashierActivity.this);
                            alertDialogBuilder.setTitle("Kembalian");
                            if(Integer.parseInt(inputCash.getText().toString()) > getTotalPrice()) {
                                alertDialogBuilder.setMessage(
                                        MoneyFormat.idr(
                                                Double.valueOf(String.valueOf(
                                                        Integer.parseInt(inputCash.getText().toString()) - getTotalPrice())
                                                )
                                        )
                                );
                            } else {
                                alertDialogBuilder.setMessage(MoneyFormat.idr(Double.valueOf("0")));
                            }
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    transactionMenus = db.getTransactionMenu(transaction.getId());
                                    db.deleteTransaction(transaction.getId());
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
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        } else {
                            if (!mBluetoothAdapter.isEnabled()) {
                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                            } else {
                                if(BLUETOOTH_PRINTER.IsNoConnection()){
                                    Intent intent = new Intent(CashierActivity.this, DeviceListActivity.class);
                                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                                }else {
                                    print(getApplicationContext(),
                                            inputCash.getText().toString(),
                                            String.valueOf(Integer.parseInt(inputCash.getText().toString()) - getTotalPrice()));
                                    dialog.dismiss();
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CashierActivity.this);
                                    alertDialogBuilder.setTitle("Kembalian");
                                    if(Integer.parseInt(inputCash.getText().toString()) > getTotalPrice()) {
                                        alertDialogBuilder.setMessage(
                                                MoneyFormat.idr(
                                                        Double.valueOf(String.valueOf(
                                                                Integer.parseInt(inputCash.getText().toString()) - getTotalPrice())
                                                        )
                                                )
                                        );
                                    } else {
                                        alertDialogBuilder.setMessage(MoneyFormat.idr(Double.valueOf("0")));
                                    }
                                    alertDialogBuilder.setCancelable(false);
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            //Do nothing
                                        }
                                    });

                                    alertDialogBuilder.setNeutralButton("Print Lagi", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            if (!mBluetoothAdapter.isEnabled()) {
                                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                                                // Otherwise, setup the chat session
                                            } else {
                                                if(BLUETOOTH_PRINTER.IsNoConnection()){
                                                    Intent intent = new Intent(CashierActivity.this, DeviceListActivity.class);
                                                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                                                }else {
                                                    print(getApplicationContext(),
                                                            inputCash.getText().toString(),
                                                            String.valueOf(Integer.parseInt(inputCash.getText().toString()) - getTotalPrice()));
                                                }
                                            }
                                        }
                                    });

                                    alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            transactionMenus = db.getTransactionMenu(transaction.getId());
                                            db.deleteTransaction(transaction.getId());
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
                                    });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                            }
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

    private void getLastId () {
        mProgressDialog.show();
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<GetLastIdResponse> call = apiService.getLastId(new Memcache(getApplicationContext()).getUser().getToken());
        call.enqueue(new Callback<GetLastIdResponse>() {
            @Override
            public void onResponse(Call<GetLastIdResponse>call, Response<GetLastIdResponse> response) {
                mProgressDialog.dismiss();
                id = response.body().getId()+1;
            }

            @Override
            public void onFailure(Call<GetLastIdResponse>call, Throwable t) {
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

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(calendar.getTime());
        return formattedDate;
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
    public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that to be enabled.
        // initializeBluetoothDevice() will then be called during onActivityResult
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
                if (BLUETOOTH_PRINTER == null){
                    initializeBluetoothDevice();
                }else{
                    if(BLUETOOTH_PRINTER.IsNoConnection()){
                        ///mImgPosPrinter.setImageResource(R.drawable.pos_printer_offliine);
                    }else{
                        //Toast.makeText(getApplicationContext(),""+R.string.title_connected_to, Toast.LENGTH_SHORT).show();
                        //txtPrinterStatus.setText(R.string.title_connected_to);
                        //txtPrinterStatus.append(device.getCategoryName());
                        //mImgPosPrinter.setImageResource(R.drawable.pos_printer);
                    }
                }
            }
        }

    }

    static class BluetoothHandler extends Handler {
        private final WeakReference<CashierActivity> myWeakReference;

        //Creating weak reference of BluetoothPrinterActivity class to avoid any leak
        BluetoothHandler(CashierActivity weakReference) {
            myWeakReference = new WeakReference<CashierActivity>(weakReference);
        }
        @Override
        public void handleMessage(Message msg)
        {
            CashierActivity bluetoothPrinterActivity = myWeakReference.get();
            if (bluetoothPrinterActivity != null) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                switch (data.getInt("flag")) {
                    case Contants.FLAG_STATE_CHANGE:
                        int state = data.getInt("state");
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + state);
                        switch (state) {
                            case HsBluetoothPrintDriver.CONNECTED_BY_BLUETOOTH:
                                //Toast.makeText(getApplicationContext(),""+R.string.title_connected_to, Toast.LENGTH_SHORT).show();
                                //txtPrinterStatus.setText(R.string.title_connected_to);
                                // txtPrinterStatus.append(device.getCategoryName());
                                StaticValue.isPrinterConnected=true;
                                Toast.makeText(CONTEXT,"Connected to device", Toast.LENGTH_SHORT).show();
                                //mImgPosPrinter.setImageResource(R.drawable.pos_printer);
                                break;
                            case HsBluetoothPrintDriver.FLAG_SUCCESS_CONNECT:
                                //txtPrinterStatus.setText(R.string.title_connecting);
                                break;

                            case HsBluetoothPrintDriver.UNCONNECTED:
                                //txtPrinterStatus.setText(R.string.no_printer_connected);
                                break;
                        }
                        break;
                    case Contants.FLAG_SUCCESS_CONNECT:
                        //txtPrinterStatus.setText(R.string.title_connecting);
                        break;
                    case Contants.FLAG_FAIL_CONNECT:
                        Toast.makeText(CONTEXT,"Connection failed.",Toast.LENGTH_SHORT).show();
                        //mImgPosPrinter.setImageResource(R.drawable.pos_printer_offliine);
                        break;
                    default:
                        break;

                }
            }
        };
    }


    private void initializeBluetoothDevice() {
        Log.d(TAG, "setupChat()");
        // Initialize HsBluetoothPrintDriver class to perform bluetooth connections
        BLUETOOTH_PRINTER = HsBluetoothPrintDriver.getInstance();//
        BLUETOOTH_PRINTER.setHandler(new BluetoothHandler(CashierActivity.this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    BLUETOOTH_PRINTER.start();
                    BLUETOOTH_PRINTER.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    initializeBluetoothDevice();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
//                    finish();
                }
        }
    }

    private void print(Context context, String cash, String change){
        Restaurant restaurant = new Memcache(getApplicationContext()).getRestaurant();
        User user = new Memcache(getApplicationContext()).getUser();

        CashierActivity.BLUETOOTH_PRINTER.Begin();
        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//CENTER
        CashierActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//30 * 0.125mm
        CashierActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x10);//normal
        CashierActivity.BLUETOOTH_PRINTER.BT_Write("\n\n"+restaurant.getName()+"\n");

        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//CENTER
        CashierActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//30 * 0.125mm
        CashierActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal
        CashierActivity.BLUETOOTH_PRINTER.BT_Write(restaurant.getAddress()+"\n");
        CashierActivity.BLUETOOTH_PRINTER.LF();

        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 0);
        CashierActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);
        CashierActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);
        //BT_Write() method will initiate the printer to start printing.
        CashierActivity.BLUETOOTH_PRINTER.BT_Write(
                "\nMeja No. " + transaction.getTableNumber() +
                        "\nNo. Transaksi: " + id +
                        "\nWaktu: " + getDate() +
                        "\nCashier: " + user.getName() +"\n");

        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));
        CashierActivity.BLUETOOTH_PRINTER.LF();
        for (int i=0; i<transactionMenus.size(); i++){
            CashierActivity.BLUETOOTH_PRINTER.LF();
            CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 0);//LEFT
            CashierActivity.BLUETOOTH_PRINTER.BT_Write(db.getMenu(transactionMenus.get(i).getMenuId()).getName()+"\n");
            CashierActivity.BLUETOOTH_PRINTER.LF();
            //CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 2);//LEFT
            CashierActivity.BLUETOOTH_PRINTER.BT_Write(StaticValue.nameLeftValueRightJustify(transactionMenus.get(i).getQty() + "x" + db.getMenu(transactionMenus.get(i).getMenuId()).getPrice(), MoneyFormat.idr(Double.valueOf(String.valueOf(db.getMenu(transactionMenus.get(i).getMenuId()).getPrice() * transactionMenus.get(i).getQty()))) , 32));
            CashierActivity.BLUETOOTH_PRINTER.BT_Write("\n");
        }

        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));
        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 2);//RIGHT
        CashierActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//50 * 0.125mm
        CashierActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte)0x00);//normal font

        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.BT_Write("Total: " + MoneyFormat.idr(Double.valueOf(String.valueOf(getTotalPrice()))) +"\n");
        CashierActivity.BLUETOOTH_PRINTER.BT_Write("Bayar Tunai: " + MoneyFormat.idr(Double.valueOf(cash))+"\n");

        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//center
        CashierActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal font
        CashierActivity.BLUETOOTH_PRINTER.BT_Write(context.getResources().getString(R.string.print_line));

        CashierActivity.BLUETOOTH_PRINTER.LF();
        CashierActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);
        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 2);//Right
        CashierActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);
        CashierActivity.BLUETOOTH_PRINTER.BT_Write("Kembalian: " + MoneyFormat.idr(Double.valueOf(change))+"\n");

        CashierActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//Center
        CashierActivity.BLUETOOTH_PRINTER.BT_Write("\n\nTerima Kasih\n");
        CashierActivity.BLUETOOTH_PRINTER.BT_Write("Silahkan datang kembali.\n\n\n");
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
