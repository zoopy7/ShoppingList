package deleon.cj.shoppinglist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ViewHolder extends RecyclerView.ViewHolder {
    public View layout;
    public TextView txtItemName;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView;
        txtItemName = itemView.findViewById(R.id.textView_ItemName);
    }

    public View getLayout() {
        return layout;
    }

    public void setLayout(View layout) {
        this.layout = layout;
    }

    public TextView getTxtItemName() {
        return txtItemName;
    }

    public void setTxtItemName(TextView txtItemName) {
        this.txtItemName = txtItemName;
    }
}
