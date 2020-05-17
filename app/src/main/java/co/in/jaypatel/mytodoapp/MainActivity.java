package co.in.jaypatel.mytodoapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText todoEditText;
    TextView emptyText;
    Button addButton;
    ListView todoListView;

    List<String> todoList;
    List<String> keyList;
    ArrayAdapter arrayAdapter;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        todoEditText = findViewById(R.id.todoEditText);
        addButton = findViewById(R.id.addButton);
        todoListView = findViewById(R.id.todoListView);
        emptyText = findViewById(R.id.emptyText);
        todoListView.setEmptyView(emptyText);

        // Initialize data references & adapters
        todoList = new ArrayList<>();
        keyList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, todoList);
        todoListView.setAdapter(arrayAdapter);

        // Initialize progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching todos!");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Initialize firebase database connection
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("todos");

        // Listen on data change event
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Dismiss the progress dialog once data loaded
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                // Clear lists
                todoList.clear();
                keyList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Add data to lists
                    todoList.add(0, snapshot.getValue(String.class));
                    keyList.add(0, snapshot.getKey());
                }

                // Notify adapter as list updated
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Perform operations when list item tapped
        todoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Delete the to-do item from the database
                myRef.child(keyList.get(position)).removeValue();
            }
        });

    }

    public void addTodo(View view) {
        // Add to-do item to the database
        if (todoEditText.getText().toString().length() > 0) {
            String todo = todoEditText.getText().toString().trim();

            // Push the to-do into database
            myRef.push().setValue(todo);

            // Clear editText data
            todoEditText.getText().clear();
        } else {
            todoEditText.setError("Please enter todo first");
        }
    }
}
