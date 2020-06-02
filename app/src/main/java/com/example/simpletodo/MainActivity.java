package com.example.simpletodo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_ITEM_TEXT ="item_text";
    public static final String KEY_ITEM_POSITION ="item_position";
    public static final int EDIT_TEXT_CODE= 20;

    List<String> items;

    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set variables by ID reference
        btnAdd= findViewById(R.id.btnAdd);
        etItem= findViewById(R.id.etItem);
        rvItems= findViewById(R.id.rvItems);

        // Create a new list of items
        /*items= new ArrayList<>();
        items.add("Buy Milk");
        items.add("Do Homework");
        items.add("Go to Dance Class");*/

        //Load items from file
        loadItems();

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position) {
                //Delete the item from the model
                items.remove(position);
                //Notify the adapter the item was indeed removed.
                itemsAdapter.notifyItemRemoved(position);
                //Notify user that the item was removed
                Toast.makeText(getApplicationContext(),"Item was removed!", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };

        ItemsAdapter.OnClickListener onClickListener= new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single click at position"+position);
                //Create new activity ( This would be unique for the item clicked on)
                Intent i= new Intent (MainActivity.this, EditActivity.class);
                //Pass all relevant data pertaining to the clicked item.
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                //Display the activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };

        // Update the parameter to now add the onLongClickListener
        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);

        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem= etItem.getText().toString();
                //Add item to the model.
                items.add(todoItem);
                //Notify adapter that an item is inserted.
                itemsAdapter.notifyItemInserted(items.size()-1);
                //Empty the text input.
                etItem.setText("");
                //Notify user that the item was added.
                Toast.makeText(getApplicationContext(),"Item was added!", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });

    }

    //Handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE){
            //Retrieve updated item
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            // Extract the original position of the edited ite from the position key.
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);
            //Update the model
            items.set(position, itemText);
            //Notify the adapter
            itemsAdapter.notifyItemChanged(position);
            //Persist the changes
            Toast.makeText(getApplicationContext(),"Item was edited!", Toast.LENGTH_SHORT).show();
            saveItems();
        }else{
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile(){
        return new File(getFilesDir(), "data.txt");
    }

    //Read the data file: The function would load items by reading every line in the file.
    private void loadItems(){
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity","Error reading Items",e);
            items= new ArrayList<>();
        }
    }
    //Write data to the file: The function the saves data to the file.
    private void saveItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity","Error writing Items",e);
        }
    }
}
