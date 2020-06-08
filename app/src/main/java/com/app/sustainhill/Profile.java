package com.app.sustainhill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.app.sustainhill.Adapters.DataAdapter;
import com.facebook.login.Login;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class Profile extends AppCompatActivity {

    private TextView reset;
    private static TextInputLayout name, amount, date;
    private static Button save, logout;
    private static RecyclerView rv;
    private static ArrayList<String> namel, amountl, datel, keys;
    private static boolean updating = false;
    private static int updatingPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            startActivity(new Intent(Profile.this, Login.class));
            finish();
        }

        reset = findViewById(R.id.reset);
        name = findViewById(R.id.name);
        amount = findViewById(R.id.amount);
        date = findViewById(R.id.date);
        save = findViewById(R.id.save);
        logout = findViewById(R.id.logout);
        rv = findViewById(R.id.data);

        namel = new ArrayList<String>();
        amountl = new ArrayList<String>();
        datel = new ArrayList<String>();
        keys = new ArrayList<String>();

        final ProgressDialog dialog = ProgressDialog.show(Profile.this, "",
                "Please wait...", true);
        dialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("data").child(user.getUid());

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        namel.add(ds.child("name").getValue().toString());
                        amountl.add(ds.child("amount").getValue().toString());
                        datel.add(ds.child("date").getValue().toString());
                        keys.add(ds.child("key").getValue().toString());
                    }
                    rv.setAdapter(null);
                    DataAdapter cd = new DataAdapter(Profile.this, namel, amountl, datel);
                    rv.setLayoutManager(new LinearLayoutManager(Profile.this));
                    rv.setAdapter(cd);
                    dialog.dismiss();
                }else{
                    dialog.dismiss();
                    Toast.makeText(Profile.this, "You haven't save any data yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("error", "Failed to read value.", error.toException());
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valid()) {
                    String namee = name.getEditText().getText().toString().trim();
                    String amountt = amount.getEditText().getText().toString().trim();
                    String datee = date.getEditText().getText().toString().trim();
                    if (updating) {
                        namel.set(updatingPos, namee);
                        amountl.set(updatingPos, amountt);
                        datel.set(updatingPos, datee);
                        myRef.child(keys.get(updatingPos)).child("name").setValue(namee);
                        myRef.child(keys.get(updatingPos)).child("amount").setValue(amountt);
                        myRef.child(keys.get(updatingPos)).child("date").setValue(datee);
                    }else{
                        namel.add(namee);
                        amountl.add(amountt);
                        datel.add(datee);
                        String key = myRef.push().getKey();
                        keys.add(key);
                        myRef.child(key).child("name").setValue(namee);
                        myRef.child(key).child("amount").setValue(amountt);
                        myRef.child(key).child("date").setValue(datee);
                        myRef.child(key).child("key").setValue(key);
                    }
                    rv.setAdapter(null);
                    DataAdapter cd = new DataAdapter(Profile.this, namel, amountl, datel);
                    rv.setLayoutManager(new LinearLayoutManager(Profile.this));
                    rv.setAdapter(cd);

                    Log.e("datat", namel + "," + amountl + ", " + datel + ", " + keys);
                    name.getEditText().setText("");
                    amount.getEditText().setText("");
                    date.getEditText().setText("");
                    updating = false;
                    save.setText("SAVE");
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name.getEditText().setText("");
                amount.getEditText().setText("");
                date.getEditText().setText("");
                updating = false;
                save.setText("SAVE");
            }
        });

        date.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker arg0, int y, int m, int d) {
                        date.getEditText().setText( d + "/" + (m + 1)+ "/" + y );
                    }
                };
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(Profile.this, myDateListener, year, month, day).show();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, UserLogIn.class));
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(Profile.this, Login.class));
            finish();
        }
    }

    boolean valid(){
        return name.getEditText().getText().toString().trim().length() != 0
                && amount.getEditText().getText().toString().trim().length() != 0
                && date.getEditText().getText().toString().trim().length() != 0;
    }

    public static void editData(int position){
        name.getEditText().setText(namel.get(position));
        amount.getEditText().setText(amountl.get(position));
        date.getEditText().setText(datel.get(position));

        save.setText("UPDATE");
        updating = true;
        updatingPos = position;

        Log.e("pos", position + "");
    }
}
