package deleon.cj.shoppinglist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    // Firebase variables
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference shoppingListNodeReference;
    private DatabaseReference itemNodeReference;
    private DatabaseReference uniqueIDNodeReference;

    private List<String> input = new ArrayList<>();
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initalize Firebase components
        firebaseDatabase = FirebaseDatabase.getInstance();
        shoppingListNodeReference = firebaseDatabase.getReference("ShoppingList");
        uniqueIDNodeReference = firebaseDatabase.getReference("ID");


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

//        for (int i = 0; i < 10; i++) {
//            input.add("Test " + i);
//        }

        firebaseDatabase.getReference().child("ShoppingList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child:
                     snapshot.getChildren()) {
                    ShoppingItem item = child.getValue(ShoppingItem.class);
                    input.add(item.item);
                }
                shoppingListNodeReference.removeEventListener(this);
                adapter = new ListAdapter(input);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        firebaseDatabase.getReference().child("UniqueID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(int.class) == null){
                    uniqueIDNodeReference.setValue(0);
                }else {
                    id = snapshot.getValue(int.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                ViewGroup viewGroup = findViewById(R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.add_item, viewGroup, false);
                final TextInputEditText textViewAddItem = dialogView.findViewById(R.id.textView_addItem);
                builder.setView(dialogView)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                input.add(Objects.requireNonNull(textViewAddItem.getText()).toString());
                                adapter.notifyItemInserted(input.size());


                                ShoppingItem shoppingItem = new ShoppingItem(id, Objects.requireNonNull(textViewAddItem.getText()).toString());
                                itemNodeReference = shoppingListNodeReference.child(shoppingItem.item + " " + shoppingItem.id);
                                itemNodeReference.setValue(shoppingItem).addOnCompleteListener(completeAddingItemListener);
                                id++;
                                uniqueIDNodeReference.setValue(id);
                                recyclerView.smoothScrollToPosition((Objects.requireNonNull(recyclerView.getAdapter()).getItemCount()));

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        input.remove(viewHolder.getAdapterPosition());
                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public OnCompleteListener<Void> completeAddingItemListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Data saved!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                Log.e(TAG, "onComplete: ERROR: " + task.getException().getLocalizedMessage());
            }
        }
    };
}