package adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.druglane.R;

import java.util.List;

import dao.ReplyDao;
import database.RoomDatabase;
import models.Reply;

/**
 * Created by Carl on 11/18/2018.
 */

public class ViewRepliesAdapter  extends RecyclerView.Adapter<ViewRepliesAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private List<Reply> mItems;
    private ReplyDao replyDao;

    public ViewRepliesAdapter(Context context) {
        RoomDatabase db = RoomDatabase.getDatabase(context);
        replyDao = db.replyDao();
        mInflater = LayoutInflater.from(context);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageView;
        private final TextView seller;
        private final TextView seller_details;
        private final TextView verified;
        private final TextView seller_region;
        private final TextView seller_physical_location;
        private final TextView date_view;

//        private final TextView senderDetailsView;

        private ViewHolder(View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.reply_message);
            seller  = itemView.findViewById(R.id.seller);
            seller_details = itemView.findViewById(R.id.seller_details);
            verified = itemView.findViewById(R.id.verified);
            seller_region = itemView.findViewById(R.id.seller_region);
            seller_physical_location = itemView.findViewById(R.id.seller_physical_location);
            date_view = itemView.findViewById(R.id.reply_date);
        }


    }

    public void setWords(List<Reply> words){
        mItems = words;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mItems != null)
            return mItems.size();
        else return 0;
    }
    @Override
    public ViewRepliesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.view_replies_layout, parent, false);
        return new ViewRepliesAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewRepliesAdapter.ViewHolder holder, int position) {

        if (mItems != null) {

            if(position %2 == 1)
            {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            else
            {
                holder.itemView.setBackgroundColor(Color.parseColor("#F7F2FF"));
                //  holder.imageView.setBackgroundColor(Color.parseColor("#f7f2ff"));
            }
            Reply current = mItems.get(position);
            String seller_details = current.getSeller_phone();
            String seller = current.getSeller_name();
            holder.messageView.setText(current.getReply_message());
            holder.seller_details.setText(seller_details);
            holder.seller.setText(seller);
            holder.seller_region.setText(current.getSeller_location());
            holder.seller_physical_location.setText(current.getPhysical_location());
            holder.date_view.setText(current.getDate());
            if(current.getVerified().equals("yes")){
                holder.verified.setVisibility(View.VISIBLE);
                holder.seller_physical_location.setVisibility(View.VISIBLE);
                holder.seller_details.setVisibility(View.VISIBLE);
            }
            else{
                holder.verified.setVisibility(View.GONE);
                //hide phone and location as well
                holder.seller_physical_location.setVisibility(View.GONE);
                holder.seller_details.setVisibility(View.GONE);
            }
//            replyDao.getMessageReplies(msg).observe((LifecycleOwner) this, new Observer<List<Reply>>() {
//                @Override
//                public void onChanged(@Nullable List<Reply> replies) {
//                    holder.repliesView.setText(String.valueOf(replies.size()));
//                }
//            });
//            int count = replyDao.getReplyCount(current.getMessage());
//            holder.repliesView.setText("");
        } else {
            // Covers the case of data not being ready yet.
            holder.messageView.setText("No Word");
        }

    }
}

