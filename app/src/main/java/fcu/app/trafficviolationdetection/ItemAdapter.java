package fcu.app.trafficviolationdetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private Context context;
    private List<items> log;
    private FirebaseStorage storage;

    public ItemAdapter(Context context, List<items> carLogs) {
        this.context = context;
        this.log = carLogs;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.MyViewHolder holder, int position) {
        items items = log.get(position);
        holder.carDate.setText(items.getCarDate());
        holder.CarPlate.setText(items.getCarPlate());
        holder.CarRule.setText(items.getCarRule());


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CarsLogDetail.class);
            intent.putExtra("reportId", items.getReportId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return log.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView carDate;
        TextView CarPlate;
        TextView CarRule;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            carDate = itemView.findViewById(R.id.Date);
            CarPlate = itemView.findViewById(R.id.CarPlate);
            CarRule = itemView.findViewById(R.id.rule);
        }
    }

}
