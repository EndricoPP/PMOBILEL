package com.example.praktikum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private FirebaseAuth auth;
    private DatabaseReference itemsRef;
    private ValueEventListener listener;
    private EditText inputKode, inputNama, inputSatuan, inputHarga;
    private final List<Item> daftar = new ArrayList<>();
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            bukaLogin();
            return;
        }

        setContentView(R.layout.activity_main);
        itemsRef = FirebaseDatabase.getInstance().getReference("items");

        inputKode = findViewById(R.id.inputKode);
        inputNama = findViewById(R.id.inputNama);
        inputSatuan = findViewById(R.id.inputSatuan);
        inputHarga = findViewById(R.id.inputHarga);

        RecyclerView recycler = findViewById(R.id.recyclerItems);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ItemAdapter(daftar, new ItemAdapter.AksiItem() {
            @Override public void edit(Item item) {
                tampilkanDialogEdit(item);
            }

            @Override public void hapus(Item item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Hapus " + item.getNama() + "?")
                        .setNegativeButton("Batal", null)
                        .setPositiveButton("Hapus", (d, w) ->
                                itemsRef.child(item.getKode()).removeValue()
                                        .addOnFailureListener(e -> pesan("Gagal hapus: " + e.getMessage()))
                        ).show();
            }
        });
        recycler.setAdapter(adapter);

        findViewById(R.id.btnSimpan).setOnClickListener(v -> simpanItem());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            auth.signOut();
            bukaLogin();
        });

        listener = itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                daftar.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    if (item != null) daftar.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pesan("Gagal membaca data: " + error.getMessage());
            }
        });
    }

    private void simpanItem() {
        String kode = isi(inputKode);
        String nama = isi(inputNama);
        String satuan = isi(inputSatuan);
        String teksHarga = isi(inputHarga);

        if (kode.isEmpty() || nama.isEmpty() || satuan.isEmpty() || teksHarga.isEmpty()) {
            pesan("Semua field wajib diisi");
            return;
        }
        if (!kodeValid(kode)) {
            pesan("Kode tidak boleh mengandung . # $ [ ] atau /");
            return;
        }

        try {
            int harga = Integer.parseInt(teksHarga);
            if (harga < 0) {
                pesan("Harga tidak boleh negatif");
                return;
            }

            Item item = new Item(kode, nama, satuan, harga);
            itemsRef.child(kode).setValue(item)
                    .addOnSuccessListener(unused -> {
                        pesan("Item tersimpan");
                        inputKode.setText("");
                        inputNama.setText("");
                        inputSatuan.setText("");
                        inputHarga.setText("");
                    })
                    .addOnFailureListener(e -> pesan("Gagal simpan: " + e.getMessage()));
        } catch (NumberFormatException e) {
            pesan("Harga harus berupa angka yang valid");
        }
    }

    private void tampilkanDialogEdit(Item item) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        form.setPadding(p, p, p, p);

        EditText kode = field(form, "Kode", item.getKode(), false);
        EditText nama = field(form, "Nama", item.getNama(), false);
        EditText satuan = field(form, "Satuan", item.getSatuan(), false);
        EditText harga = field(form, "Harga", String.valueOf(item.getHarga()), true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Item")
                .setView(form)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", null)
                .create();

        dialog.setOnShowListener(unused ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String kodeBaru = isi(kode);
                    String namaBaru = isi(nama);
                    String satuanBaru = isi(satuan);
                    String teksHarga = isi(harga);

                    if (kodeBaru.isEmpty() || namaBaru.isEmpty()
                            || satuanBaru.isEmpty() || teksHarga.isEmpty()) {
                        pesan("Semua field wajib diisi");
                        return;
                    }
                    if (!kodeValid(kodeBaru)) {
                        pesan("Kode tidak boleh mengandung . # $ [ ] atau /");
                        return;
                    }

                    try {
                        int hargaBaru = Integer.parseInt(teksHarga);
                        if (hargaBaru < 0) {
                            pesan("Harga tidak boleh negatif");
                            return;
                        }

                        Item hasil = new Item(kodeBaru, namaBaru, satuanBaru, hargaBaru);

                        if (kodeBaru.equals(item.getKode())) {
                            itemsRef.child(kodeBaru).setValue(hasil)
                                    .addOnSuccessListener(x -> dialog.dismiss())
                                    .addOnFailureListener(e -> pesan("Gagal edit: " + e.getMessage()));
                        } else {
                            // Pindahkan key lama ke key baru dalam satu update.
                            Map<String, Object> perubahan = new HashMap<>();
                            perubahan.put(item.getKode(), null);
                            perubahan.put(kodeBaru, hasil);
                            itemsRef.updateChildren(perubahan)
                                    .addOnSuccessListener(x -> dialog.dismiss())
                                    .addOnFailureListener(e -> pesan("Gagal edit: " + e.getMessage()));
                        }
                    } catch (NumberFormatException e) {
                        pesan("Harga harus berupa angka yang valid");
                    }
                })
        );
        dialog.show();
    }

    private EditText field(LinearLayout form, String hint, String nilai, boolean angka) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(nilai);
        if (angka) input.setInputType(InputType.TYPE_CLASS_NUMBER);
        form.addView(input);
        return input;
    }

    private String isi(EditText input) {
        return input.getText().toString().trim();
    }

    private boolean kodeValid(String kode) {
        return !kode.matches(".*[.#$\\[\\]/].*");
    }

    private void bukaLogin() {
        startActivity(new Intent(this, activity_login.class));
        finish();
    }

    private void pesan(String teks) {
        Toast.makeText(this, teks, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (itemsRef != null && listener != null) {
            itemsRef.removeEventListener(listener);
        }
        super.onDestroy();
    }
}