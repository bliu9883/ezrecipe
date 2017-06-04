package com.example.nimira.ezrecipe;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    ArrayList<String> selection = new ArrayList<String>();
    ArrayList<String> recipeIDs = new ArrayList<>();
    ArrayList<String> recipeNames = new ArrayList<>();
    ArrayList<String> recipeImages = new ArrayList<>();
    ArrayList<String> addedIngredients = new ArrayList<>();
    Button ingredients, addIngredients, getFood, done, delete, login;
    EditText search;
    LinearLayout linearMain;
    CheckBox checkBox;
    String email, uid;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        linearMain = (LinearLayout) findViewById(R.id.buttons);
        search = (EditText) findViewById(R.id.search);
        getFood = (Button) findViewById(R.id.getFood);
        done = (Button) findViewById(R.id.done);
        ingredients = (Button) findViewById(R.id.ingredients);
        delete = (Button) findViewById(R.id.delete);
        login = (Button) findViewById(R.id.login);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            email = user.getEmail();
            Log.i("email", email);
            uid = user.getUid();
            Log.i("uid", uid);
        } else {
            // No user is signed in
            Log.i("email", "none");
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> ingredients = new ArrayList<>();
                for (DataSnapshot i: dataSnapshot.getChildren()){
                    IngredientsList list = i.getValue(IngredientsList.class);
                    if (list.getUserId().equals(uid)) {
                        ingredients = list.getIngredients();
                    }
                }
                Log.i("ingredients", String.valueOf(ingredients));
//                addedIngredients.clear();
                addedIngredients = ingredients;
                if (addedIngredients!=null) {
                    displayCheckBoxes();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ingredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HttpResponse<JsonNode> response = new CallMashapeAsync().execute().get();
                    String data = response.getBody().toString();
                    Log.i("data", data);
                    JSONArray root = new JSONArray(data);
                    for (int i = 0; i < root.length(); i++) {
                        recipeIDs.add(root.getJSONObject(i).getString("id"));
                        recipeNames.add(root.getJSONObject(i).getString("title"));
                        recipeImages.add(root.getJSONObject(i).getString("image"));
                    }
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    intent.putExtra("recipeIDs", recipeIDs);
                    intent.putExtra("recipeNames", recipeNames);
                    intent.putExtra("recipeImage", recipeImages);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                recipeIDs.clear();
                recipeNames.clear();
                recipeImages.clear();
            }
        });

        addIngredients = (Button) findViewById(R.id.addIngredients);
        addIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredients.setVisibility(View.GONE);
                search.setVisibility(View.VISIBLE);
                getFood.setVisibility(View.VISIBLE);
                done.setVisibility(View.VISIBLE);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addIngredients.setVisibility(View.VISIBLE);
                search.setVisibility(View.GONE);
                getFood.setVisibility(View.GONE);
                done.setVisibility(View.GONE);
                search.setText(null);
            }
        });

        getFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addedIngredients.add(search.getText().toString());
                Collections.sort(addedIngredients, String.CASE_INSENSITIVE_ORDER);
                Log.i("addedIngredients array", addedIngredients.toString());
                displayCheckBoxes();
                search.setText(null);

                IngredientsList food = new IngredientsList(addedIngredients, uid, email);
                mDatabase.child(uid).setValue(food);

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectItems(v);
                for (int i = 0; i < selection.size(); i++) {
                    addedIngredients.remove(selection.get(i));
                }
                Log.i("items deleted", selection.toString());
                Log.i("ingredients left", addedIngredients.toString());
                selection.clear();
                
                displayCheckBoxes();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    public void displayCheckBoxes(){
        linearMain.removeAllViewsInLayout();
        for (int i=0; i<addedIngredients.size(); i++){
            checkBox = new CheckBox(MainActivity.this);
            checkBox.setId(i);
            checkBox.setText(addedIngredients.get(i));
            checkBox.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    selectItems(v);
                }
            });
            linearMain.addView(checkBox);
        }
    }

    public ArrayList<CheckBox> getCheckBoxes(){
        ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();
        ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
        findCheckBoxes(viewGroup, checkBoxes);
        return checkBoxes;
    }


    private static void findCheckBoxes(ViewGroup viewGroup, ArrayList<CheckBox> checkBoxes) {
        for (int i=0, N = viewGroup.getChildCount(); i<N; i++){
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup){
                findCheckBoxes((ViewGroup) child, checkBoxes);
            }
            else if (child instanceof CheckBox) {
                checkBoxes.add((CheckBox) child);
            }
        }
    }
    public void selectItems(View v) {
        ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();
        ArrayList<String> checkedIngredients = new ArrayList<String>();
        checkBoxes = getCheckBoxes();

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isChecked()) {
                checkedIngredients.add(checkBoxes.get(i).getText().toString());
            } else {
                checkedIngredients.remove(checkBoxes.get(i).getText().toString());
            }
        }
        Log.i("List", checkedIngredients.toString());
        selection = checkedIngredients;
    }


    private class CallMashapeAsync extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {

        protected HttpResponse<JsonNode> doInBackground(String... msg) {
            String items = "";
            for (int i=0; i<selection.size(); i++){
                items = items + "" + selection.get(i) + "%2C";
            }
            String url = "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/findByIngredients?fillIngredients=false&ingredients="+items+ "&limitLicense=false&number=10&ranking=1";
            Log.i("url: ", url);
            HttpResponse<JsonNode> request = null;
            try {

                request = Unirest.get(url)
                        .header("X-Mashape-Key", "gNrvLXTPTNmshsXWUXLzm7VwvkJWp1m47mVjsn5eRbKVitWD4i")
                        .header("Accept", "application/json")
                        .asJson();
//                Log.i("request", "" + request);
            } catch (UnirestException e) {
                // TO8DO Auto-generated catch block
                e.printStackTrace();
            }

            return request;
        }


        protected void onProgressUpdate(Integer...integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {

        }
    }
}