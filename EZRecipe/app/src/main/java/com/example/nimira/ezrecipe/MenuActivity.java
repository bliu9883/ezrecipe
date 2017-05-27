package com.example.nimira.ezrecipe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by miggycalleja on 5/15/17.
 */

public class MenuActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        text = (TextView)findViewById(R.id.secondPage);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Intent intent = getIntent();
        ArrayList<String> recipeIDs = (ArrayList<String>) intent.getSerializableExtra("recipeIDs");
        ArrayList<String> recipeNames = (ArrayList<String>) intent.getSerializableExtra("recipeNames");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        //sets value, adds list of new recipe searches to previous list.
        ref.setValue(recipeIDs);
        ref.push().setValue(recipeIDs);
//        Button recipe1 = (Button)findViewById(R.id.recipe1);
//        recipe1.setText(recipeNames.get(0));
        Spinner recipes = (Spinner)findViewById(R.id.recipeDropdown);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, recipeNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipes.setAdapter(dataAdapter);

    }




}
