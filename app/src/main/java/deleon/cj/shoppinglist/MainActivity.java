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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
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
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private List<ShoppingItem> shoppingItemList = new ArrayList<>();
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase components
        firebaseDatabase = FirebaseDatabase.getInstance();
        shoppingListNodeReference = firebaseDatabase.getReference("ShoppingList");
        uniqueIDNodeReference = firebaseDatabase.getReference("UniqueID");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));


        FirebaseRecyclerOptions<ShoppingItem> options = new FirebaseRecyclerOptions.Builder<ShoppingItem>()
                .setQuery(shoppingListNodeReference, new SnapshotParser<ShoppingItem>() {
                    @NonNull
                    @Override
                    public ShoppingItem parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new ShoppingItem(snapshot.child("id").getValue(int.class),
                                snapshot.child("item").getValue(String.class));
                    }
                }).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ShoppingItem, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ShoppingItem model) {
                holder.txtItemName.setText(model.item);
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new view
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View v = inflater.inflate(R.layout.items, parent, false);
                return new ViewHolder(v);
            }
        };


        firebaseDatabase.getReference().child("ShoppingList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child:
                     snapshot.getChildren()) {
                    ShoppingItem item = child.getValue(ShoppingItem.class);
                    shoppingItemList.add(item);
                }
                shoppingListNodeReference.removeEventListener(this);
                adapter = new ListAdapter(shoppingItemList);
                //recyclerView.setAdapter(adapter);
                recyclerView.setAdapter(firebaseRecyclerAdapter);
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
                                ShoppingItem shoppingItem = new ShoppingItem(id, Objects.requireNonNull(textViewAddItem.getText()).toString());
                                shoppingItemList.add(shoppingItem);
//                                adapter.notifyItemInserted(shoppingItemList.size());
                                firebaseRecyclerAdapter.notifyItemInserted(shoppingItemList.size());

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
                        ShoppingItem item = shoppingItemList.get(viewHolder.getAdapterPosition());
                        shoppingItemList.remove(viewHolder.getAdapterPosition());
//                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        firebaseRecyclerAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                        String name = String.format("%s %d",item.item,item.id);
                        shoppingListNodeReference.child(name).removeValue();

                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
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