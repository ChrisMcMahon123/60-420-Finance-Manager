package com.mcmah113.mcmah113expensesiq;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class Login extends AppCompatActivity {
    private Toolbar toolbarCustom;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //database SQL helper object
        final DatabaseHelper databaseHelper = new DatabaseHelper(this);

        //set toolbar properties
        toolbarCustom = findViewById(R.id.toolbarCustom);
        setSupportActionBar(toolbarCustom);
        ImageView imageIcon = findViewById(R.id.imageViewLogo);

        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("wallpaper.jpg");
            imageIcon.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //set username / login editText properties
        final EditText editTextUsername = findViewById(R.id.editTextAccountName);
        final EditText editTextPassword = findViewById(R.id.editTextPassword);

        //set users username and password they typed in the sign up activity
        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            editTextUsername.setText(bundle.getString("username"));
            editTextPassword.setText(bundle.getString("password"));
        }

        //set login / sign up button properties
        final CustomOnTouchListener onTouchListener = new CustomOnTouchListener(getResources().getColor(R.color.colorPrimaryDark, getTheme()));

        final Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnTouchListener(onTouchListener);//ignore this warning...
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                int userId = databaseHelper.userLogin(username, password);

                if(userId > 0) {
                    //valid login
                    Toast.makeText(Login.this, "Logged in as " + username + " ID: "+ userId, Toast.LENGTH_SHORT).show();

                    //launch account overview activity
                    Intent intent = new Intent(view.getContext(), Overview.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(Login.this, "Username / password incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button buttonSignUp = findViewById(R.id.buttoSignUp);
        buttonSignUp.setOnTouchListener(onTouchListener);//ignore this warning...
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //launch the sign up activity
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();

                Intent intent = new Intent(view.getContext(), SignUp.class);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });
    }

    public void onBackPressed() {
        hideKeyboard(toolbarCustom);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if(inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}