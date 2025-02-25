//package asliborneo.router.HistoryRecyclerView;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import asliborneo.router.R;
//
//
//import java.util.List;
//
//
//public class HistoryAdapter extends RecyclerView.Adapter<asliborneo.router.HistoryRecyclerView.HistoryViewHolders> {
//
//    private List<asliborneo.router.HistoryRecyclerView.HistoryObject> itemList;
//    private Context context;
//
//    public HistoryAdapter(List<HistoryObject> itemList, Context context) {
//        this.itemList = itemList;
//        this.context = context;
//    }
//
//    @Override
//    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
//
//        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
//        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutView.setLayoutParams(lp);
//        HistoryViewHolders rcv = new HistoryViewHolders(layoutView);
//        return rcv;
//    }
//
//    @Override
//    public void onBindViewHolder(HistoryViewHolders holder, final int position) {
//        holder.rideId.setText(itemList.get(position).getRideId());
//        if(itemList.get(position).getTime()!=null){
//            holder.time.setText(itemList.get(position).getTime());
//        }
//    }
//    @Override
//    public int getItemCount() {
//        return this.itemList.size();
//    }
//
//}