package fcu.app.trafficviolationdetection;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private Context context;
    private List<items> log;

    public ItemAdapter(Context context, List<items> carLogs) {
        this.context = context;
        this.log = carLogs;
    }

    @NonNull
    @Override
    public ItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.MyViewHolder holder, int position) {
        items item = log.get(position);
        holder.carDate.setText(item.getCarDate());
        holder.carPlate.setText(item.getCarPlate());
        holder.carRule.setText(item.getCarRule());

        // 添加日誌輸出以檢查 reportId
        Log.d("ItemAdapter", "Report ID for position " + position + ": " + item.getReportId());


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CarsLogDetail.class);
            intent.putExtra("reportId", item.getReportId());
            // 再次檢查即將傳遞的 reportId
            Log.d("ItemAdapter", "Sending reportId: " + item.getReportId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return log.size();
    }

    public void updateData(List<items> newData) {
        this.log = newData;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView carDate;
        TextView carPlate;
        TextView carRule;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            carDate = itemView.findViewById(R.id.Date);
            carPlate = itemView.findViewById(R.id.CarPlate);
            carRule = itemView.findViewById(R.id.rule);
        }
    }
}
