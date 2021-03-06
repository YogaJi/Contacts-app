package site.yogaji.contactapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import android.widget.TextView;
import android.widget.Toast;
import site.yogaji.contactapp.model.Contact;
import site.yogaji.contactapp.customdialog.ContactDialog;
import site.yogaji.contactapp.customdialog.ActionDialog;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button addPersonBtn;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Contact> contactArrayList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private EditText inputSearchNameEt;

    //set sharedPreference:
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up preference manager to xml preference
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //set up recycler view
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //set up database
        databaseHelper = new DatabaseHelper(this);
        mAdapter = new MyRecyclerViewAdapter(contactArrayList);
        recyclerView.setAdapter(mAdapter);

        //set up asyncTask
        GetAllContactAsyncTask getAllContactAsyncTask = new GetAllContactAsyncTask(this);
        getAllContactAsyncTask.execute();

        //set recycler view item touch listener
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        ActionDialog actionDialog = new ActionDialog(MainActivity.this);
                        Contact contact = contactArrayList.get(position);
                        actionDialog.createDialogAndShow(contact, new IGenerateContactListener(){
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void getContact(Contact contact) {
                                if (contact != null) {
                                    contactArrayList.set(position, contact);
                                } else {
                                    contactArrayList.remove(position);
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }));

        //set search button listener
        Button startSearchBtn = findViewById(R.id.start_search_btn);
        startSearchBtn.setOnClickListener(this);

        Button addBtn = findViewById(R.id.add_person_btn);
        addBtn.setOnClickListener(this);

        inputSearchNameEt = findViewById(R.id.search_input_name_et);
        inputSearchNameEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                //handle user input enter action on inputSearchNameEt
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    startQueryDatabase();
                }
                return false;
            }
        });


    }//end of onCreate

    //set on click
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_person_btn:

                //commit the new contact
                new ContactDialog(MainActivity.this, OperationTypeEnum.INSERT,
                        new IGenerateContactListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void getContact(Contact contact) {
                                contactArrayList.add(contact);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                break;
            case R.id.start_search_btn:
                //query database by the name input by user
                startQueryDatabase();
                break;
            default:
                break;
        }
    }
    //set query database func
    private void startQueryDatabase() {
        QueryByContactNameAsyncTask queryByContactNameAsyncTask = new QueryByContactNameAsyncTask(this);
        queryByContactNameAsyncTask.execute(inputSearchNameEt.getText().toString());
    }
    //hide keyboard func
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //life cycle to set data onResume
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    //life cycle to close database on destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
    }

    //set async task
    private static class GetAllContactAsyncTask extends AsyncTask<Void, Void, ArrayList<Contact>> {

        private final WeakReference<MainActivity> activityWeakReference;

        GetAllContactAsyncTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Contact> doInBackground(Void... voids) {
            ArrayList<Contact> contacts = new ArrayList<>();

            MainActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null) {
                contacts = new DatabaseHelper(mainActivity).getAllContact();
            }
            return contacts;
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> contacts) {
            super.onPostExecute(contacts);
            MainActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null && !mainActivity.isFinishing()) {
                mainActivity.contactArrayList = contacts;
                RecyclerView recyclerView = mainActivity.findViewById(R.id.my_recycler_view);
                mainActivity.mAdapter = new MyRecyclerViewAdapter(contacts);
                recyclerView.setAdapter(mainActivity.mAdapter);
            }
        }
    }//end of async task
    //set query
    private static class QueryByContactNameAsyncTask extends AsyncTask<String, Void, Contact> {

//        private WeakReference<MainActivity> activityWeakReference;
        private final WeakReference<MainActivity> activityWeakReference;

        QueryByContactNameAsyncTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Contact doInBackground(String... strings) {
            String inputSearchNameString = strings[0];
            MainActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null && !mainActivity.isFinishing()) {
                return new DatabaseHelper(mainActivity).getContactQueryByName(inputSearchNameString);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Contact contact) {
            super.onPostExecute(contact);
            MainActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null && !mainActivity.isFinishing()) {
                if (contact != null) {
                    //bug
                   new ContactDialog(mainActivity, OperationTypeEnum.QUERY, contact);
                    mainActivity.inputSearchNameEt.setText("");
                    Toast.makeText(mainActivity,
                            "Find!",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(mainActivity,
                            "The people is not in the list",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                mainActivity.hideSoftKeyboard();
            }
        }
    }//end of query

    //make menu:
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
    //select menu
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_settings:
                startActivity(new Intent(
                        getApplicationContext(),
                        SettingActivity.class
                ));
                break;

            case R.id.menu_About:
                Toast.makeText(this, "A00246407 Yujia Ji", Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}//end of the main class
