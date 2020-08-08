package deleon.cj.shoppinglist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<ShoppingItem> values;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View layout;
        public TextView txtItemName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView;
            txtItemName = itemView.findViewById(R.id.textView_ItemName);
        }
    }

    public void add(int position, ShoppingItem item){
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    public ListAdapter(List<ShoppingItem> dataset){
        values = dataset;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.items, parent, false);

        // Set the view's size, margins, padding, and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ListAdapter.ViewHolder holder, int position) {
        // Get element from your dataset at this position
        // replace the contents of the view with that element
        ShoppingItem item = values.get(position);
        holder.txtItemName.setText(item.item);
//        holder.txtItemName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                remove(holder.getAdapterPosition());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}
