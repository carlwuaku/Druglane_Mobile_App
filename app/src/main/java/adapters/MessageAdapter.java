package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MessageAdapter extends
        RecyclerView.Adapter<MessageAdapter.ViewHolder>
        implements Filterable {
    private final LayoutInflater mInflater;
    private List<Message> mItems;
    private static ClickListener clickListener;
    public static List<Message> filteredItems;
    public List<Message> selectedItems = new ArrayList<>();
    public static Context ctx;

    public MessageAdapter(Context context) {
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


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{
        private final TextView messageView, view_replies_btn, view_image_btn, num_unread,
                status_pending, status_done, item_blocked, date_view;
//        private final TextView repliesView;
//        private final TextView unreadView;
//        private final TextView fileView;
        LinearLayout rootView;


        private ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);

            messageView = itemView.findViewById(R.id.message);
            num_unread = itemView.findViewById(R.id.num_unread);
            item_blocked = itemView.findViewById(R.id.item_blocked);
//            fileView  = itemView.findViewById(R.id.files);
//            unreadView = itemView.findViewById(R.id.unread);
            view_replies_btn = itemView.findViewById(R.id.view_replies_btn);
            view_image_btn = itemView.findViewById(R.id.m_view_image_btn);
            status_done = itemView.findViewById(R.id.status_done);
            status_pending = itemView.findViewById(R.id.status_pending);
            date_view = itemView.findViewById(R.id.dateView);


            view_image_btn.setOnClickListener(this);
            view_replies_btn.setOnClickListener(this);
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
                       System.out.println("button clicked");
            if(view.getId() == view_image_btn.getId()){
                System.out.println("image button clicked");
                //show the image
                Intent i = new Intent(ctx, ViewImage.class);
                Message m =
                        MessageAdapter.filteredItems.get(getAdapterPosition());
                i.putExtra("message", m.getMessage());
                i.putExtra("imageUrl", m.getFilename());
                ctx.startActivity(i);
            }
//            else if(view.getId() == view_replies_btn.getId()){
//                System.out.println("replies button clicked");
//                //show the image
//                Intent i = new Intent(ctx, ViewRepliesActivity.class);
//                Message m =
//                        MessageAdapter.filteredItems.get(getAdapterPosition());
//                i.putExtra("message", m.getMessage());
//                ctx.startActivity(i);
//            }

            else{
                if(clickListener != null) {
                    System.out.println("button clicked. doing something extra");
                    clickListener.onItemClick(getAdapterPosition(), view);
                }

            }
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onItemLongClick(getAdapterPosition(), view);
            return true;
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

    public List<Message> getItems() {


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
        View itemView = mInflater.inflate(R.layout.messages_recyclerview_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (filteredItems != null) {
            Message current = filteredItems.get(position);
            String msg = current.getMessage();
            String filename = current.getFilename();
            holder.date_view.setText(current.getDate());
            holder.messageView.setText(current.getMessage());

            //if the message is blocked, show the lock
            if(current.getBlocked().equals("yes")){
                holder.item_blocked.setVisibility(View.VISIBLE);
            }
            else{
                holder.item_blocked.setVisibility(View.GONE);
            }
            holder.num_unread.setText( String.valueOf( current.getNum_unread()) );
            holder.view_replies_btn.setText(String.valueOf( current.getTotal_replies()));
//            holder.fileView.setText(filename);
//            FILE ATTACHMENT
            if(!Objects.equals(filename, "")){
                holder.view_image_btn.setVisibility(View.VISIBLE);
            }
            else{
                holder.view_image_btn.setVisibility(View.GONE);
            }
//            SELECTION
            if(current.isSelected()){
                holder.rootView.setBackgroundColor(Color.LTGRAY);
            }
            else{
                holder.rootView.setBackgroundColor(Color.TRANSPARENT);
            }
//            UNREAD
            if(current.getNum_unread() > 0){
                holder.num_unread.setVisibility(View.VISIBLE);
            }
            else{
                holder.num_unread.setVisibility(View.GONE);
            }
//              STATUS
            if(Objects.equals(current.getStatus(), "sent")){
                holder.status_pending.setVisibility(View.GONE);
                holder.status_done.setVisibility(View.VISIBLE);
            }
            else{
                holder.status_pending.setVisibility(View.VISIBLE);
                holder.status_done.setVisibility(View.GONE);
            }

//            holder.repliesView.setText(current.getLast_reply());
        } else {
            // Covers the case of data not being ready yet.
            holder.messageView.setText("No Word");
        }

    }

    ////////CLICK LISTENER///////////

    public void setOnItemClickListener(ClickListener clickListener){
        MessageAdapter.clickListener = clickListener;
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
