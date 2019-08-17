package com.martinmarinkovic.partytime;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class SearchByAtributes extends AppCompatActivity {

    private Spinner spinner;
    private RadioButton radioButton;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_atributes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Filters");

        spinner = (Spinner) findViewById(R.id.spinner);
        radioGroup = (RadioGroup) findViewById(R.id.rg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {

            String radius = spinner.getSelectedItem().toString();
            int selectedButtonId = radioGroup.getCheckedRadioButtonId();
            radioButton = (RadioButton) findViewById(selectedButtonId);
            String type = radioButton.getText().toString();

            Bundle searchBundle = new Bundle();
            searchBundle.putString("radius", radius);
            searchBundle.putString("type", type);
            //Intent i = new Intent(SearchByAtributes.this, GoogleMapsActivity.class);
            Intent i = new Intent(SearchByAtributes.this, SearchList.class);
            i.putExtras(searchBundle);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

}
