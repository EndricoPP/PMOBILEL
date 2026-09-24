package com.example.praktikum;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.Holder> {
    public interface AksiItem {
        void edit(Item item);

        void hapus(Item item);
    }

    private final List<Item> daftar;
    private final AksiItem aksi;

    public ItemAdapter(List<Item> daftar, AksiItem aksi) {
        this.daftar = daftar;
        this.aksi = aksi;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView teks;

        Holder(TextView teks) {
            super(teks);
            this.teks = teks;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView teks = new TextView(parent.getContext());
        int p = (int) (16 * parent.getResources().getDisplayMetrics().density);
        teks.setPadding(p, p, p, p);
        teks.setTextSize(16);
        return new Holder(teks);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Item item = daftar.get(position);
        holder.teks.setText(
                item.getKode() + " — " + item.getNama() +
                        "\n" + item.getSatuan() + " | Rp" + item.getHarga()
        );
        holder.itemView.setOnClickListener(v -> aksi.edit(item));
        holder.itemView.setOnLongClickListener(v -> {
            aksi.hapus(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return daftar.size();
    }
}
