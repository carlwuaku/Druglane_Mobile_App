package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.druglane.R;
import com.druglane.ViewImage;
import com.druglane.ViewRepliesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import database.RoomDatabase;
import models.Message;

/**
 * Created by Carl on 11/15/2018.
 */

public class SearchesAdapter extends RecyclerView.Adapter<SearchesAdapter.ViewHolder>
        implements Filterable {
    private final LayoutInflater mInflater;
    private static List<Message> mItems;
    private static ClickListener clickListener;
    private List<Message> selectedItems = new ArrayList<>();
    public static List<Message> filteredItems;
    public static Context ctx;

    public SearchesAdapter(Context context) {
        RoomDatabase db = RoomDatabase.getDatabase(context);
        mInflater = LayoutInflater.from(context);
        ctx = context;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredItems = mItems;
                }
                else{
                    List<Message> filteredList = new ArrayList<>();
                    for (Message row : mItems) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name match
                        if (row.getMessage().toLowerCase().contains(charString.toLowerCase()) ) {
                            filteredList.add(row);
                        }
                    }

                    filteredItems = filteredList;
                }
                filterResults.values = filteredItems;
                filterResults.count = filteredItems.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredItems = (ArrayList<Message>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public static class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener, View.OnLongClickListener{
        private final TextView messageView;
        private final TextView buyerView;
        private final TextView  view_image_btn;
        private final TextView quantityView;
        private final TextView price_filterView;
        private final TextView dateView;
        LinearLayout rootView;

        private ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            itemView.setOnClickListener(this);
            messageView = itemView.findViewById(R.id.message);
            buyerView = itemView.findViewById(R.id.buyer_name);
            quantityView = itemView.findViewById(R.id.new_message_quantity);
            price_filterView = itemView.findViewById(R.id.price_filter_view);
            dateView = itemView.findViewById(R.id.dateView);
//            reply_btn = itemView.findViewById(R.id.reply_btn);
            view_image_btn = itemView.findViewById(R.id.view_image_btn);
            view_image_btn.setOnClickListener(this);
//            reply_btn.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int p=getLayoutPosition();
                    clickListener.onItemLongClick(p, view);
                    System.out.println("LongClick: "+p);
                    return true;// returning true instead of false, works for me
                }
            });
        }

        @Override
        public void onClick(View view) {
            //when user makes selection quickly, this becomes set to null for some reason, and the app crashes

            if(view.getId() == view_image_btn.getId()){
                System.out.println("image button clicked");
                //show the image
                Intent i = new Intent(ctx, ViewImage.class);
                Message m =
                        SearchesAdapter.filteredItems.get(getAdapterPosition());
                i.putExtra("message", m.getMessage());
                i.putExtra("imageUrl", m.getFilename());
                ctx.startActivity(i);
            }

            else{
                //its the whole thing that's being clicked. user probably wants to select
                if(clickListener != null)
                    clickListener.onItemClick(getAdapterPosition(), view);

            }
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onItemLongClick(getAdapterPosition(), view);
            return false;
        }


    }

    public void setWords(List<Message> words){
        filteredItems =  mItems = words;
        notifyDataSetChanged();
    }

    public Message getWord(int position){
        return filteredItems.get(position);
    }

    public List<Message> getSelectedItems() {


        return selectedItems;
    }

    public List<Message> getFilteredItems() {


        return filteredItems;
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (filteredItems != null)
            return filteredItems.size();
        else return 0;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.searches_recyclerview_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (filteredItems != null) {
            Message current = filteredItems.get(position);
            String msg = current.getMessage();
            holder.messageView.setText(current.getMessage());
            holder.buyerView.setText(current.getSender_name());
            holder.quantityView.setText("Quantity: "+current.getQuantity());
            holder.dateView.setText(current.getDate());
//            holder.fileView.setText(current.getFilename());
            String filename = current.getFilename();
            if(!Objects.equals(filename, "")){
                holder.view_image_btn.setVisibility(View.VISIBLE);
            }
            else{
                holder.view_image_btn.setVisibility(View.GONE);
            }
            if(current.isSelected()){
                holder.rootView.setBackgroundColor(Color.LTGRAY);
            }
            else{
                holder.rootView.setBackgroundColor(Color.TRANSPARENT);
            }
            if(current.getHas_unread().equals("yes")){
                holder.messageView.setTextColor(Color.BLUE);
                holder.messageView.setTypeface(null, Typeface.BOLD);
            }
            if(!current.getPrice_filter().equals("all")){
                holder.price_filterView.setVisibility(View.VISIBLE);
                holder.price_filterView.setText(current.getPrice_filter() +" prices only");
            }
            else{
                holder.price_filterView.setVisibility(View.GONE);
            }

//            holder.repliesView.setText(current.getLast_reply());
        } else {
            // Covers the case of data not being ready yet.
            holder.messageView.setText("No Word");
        }

    }

    ////////CLICK LISTENER///////////

    public void setOnItemClickListener(ClickListener clickListener){
        SearchesAdapter.clickListener = clickListener;
    }

    public interface ClickListener{
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }


    public void setSelectedIds(List<Message> selectedIds) {
        this.selectedItems = selectedIds;
        notifyDataSetChanged();
    }


}
