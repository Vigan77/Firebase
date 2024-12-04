package coba.paba.firebase

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var _etProvinsi: EditText
    private lateinit var _etIbukota: EditText
    private lateinit var _btSimpan: Button
    private lateinit var _lvData: ListView

    private val db = Firebase.firestore
    private var data: MutableList<Map<String, String>> = ArrayList()
    private lateinit var lvAdapter: SimpleAdapter
    private var DataProvinsi = ArrayList<daftarProvinsi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        _etProvinsi = findViewById(R.id.etProvinsi)
        _etIbukota = findViewById(R.id.etIbukota)
        _btSimpan = findViewById(R.id.btSimpan)
        _lvData = findViewById(R.id.lvData)

        lvAdapter = SimpleAdapter(
            this,
            data,
            android.R.layout.simple_list_item_2,
            arrayOf("Pro", "Ibu"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        _lvData.adapter = lvAdapter

        // Event klik tombol simpan
        _btSimpan.setOnClickListener {
            val provinsi = _etProvinsi.text.toString()
            val ibukota = _etIbukota.text.toString()
            if (provinsi.isNotEmpty() && ibukota.isNotEmpty()) {
                tambahData(db, provinsi, ibukota)
            } else {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            }
        }

        // Klik lama untuk menghapus data
        _lvData.setOnItemLongClickListener { parent, view, position, id ->
            val namaPro: String? = data[position]["Pro"]
            if (namaPro != null) {
                db.collection("tbProvinsi")
                    .document(namaPro)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("Firebase", "Berhasil diHAPUS")
                        readData(db) // Refresh data setelah penghapusan
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", e.message.toString())
                    }
            }
            true
        }

        // Baca data saat aplikasi dijalankan
        readData(db)
    }

    // Fungsi untuk menambah data ke Firebase
    private fun tambahData(db: FirebaseFirestore, provinsi: String, ibukota: String) {
        val dataBaru = daftarProvinsi(provinsi, ibukota)
        db.collection("tbProvinsi")
            .document(provinsi)
            .set(dataBaru)
            .addOnSuccessListener {
                _etProvinsi.setText("")
                _etIbukota.setText("")
                Log.d("Firebase", "Data Berhasil Disimpan")
                readData(db)
            }
            .addOnFailureListener {
                Log.d("Firebase", it.message.toString())
            }
    }

    // Fungsi untuk membaca data dari Firebase
    private fun readData(db: FirebaseFirestore) {
        db.collection("tbProvinsi").get()
            .addOnSuccessListener { result ->
                data.clear()
                DataProvinsi.clear()
                for (document in result) {
                    val provinsi = document.getString("provinsi") ?: ""
                    val ibukota = document.getString("ibukota") ?: ""
                    val readData = daftarProvinsi(provinsi, ibukota)
                    DataProvinsi.add(readData)

                    val dt: MutableMap<String, String> = HashMap(2)
                    dt["Pro"] = provinsi
                    dt["Ibu"] = ibukota
                    data.add(dt)
                }
                lvAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.d("Firebase", it.message.toString())
            }
    }
}
