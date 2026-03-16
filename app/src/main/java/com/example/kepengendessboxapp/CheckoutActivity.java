// CheckoutActivity.java Berhasil
package com.example.kepengendessboxapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CheckoutPrefs";
    private static final String KEY_ADDRESS = "address";
    private EditText edtJalan, edtDetail;
    private Button btnSimpan, btnBuatPesanan;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> cartItemList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser user;
    private String userId, namaUser;
    private String namaLengkap = "Pelanggan"; // Default
    private RadioGroup radioGroupPembayaran;
    private RadioButton rbCOD, rbBCA, rbMandiri, rbBRI, rbBNI;
    private int ongkir = 5000;

    // Spinner
    private Spinner spinnerProvinsi, spinnerKota, spinnerKecamatan, spinnerKodePos;

    // Data Wilayah
    private List<String> provinsiList = new ArrayList<>();
    private List<String> kotaList = new ArrayList<>();
    private List<String> kecamatanList = new ArrayList<>();
    private List<String> kodePosList = new ArrayList<>();

    // Mapping data: KOTA → KECAMATAN
    private Map<String, List<String>> kotaToKecamatan = new HashMap<>();
    private Map<String, List<String>> kecamatanToKodePos = new HashMap<>();

    // Dummy item untuk hint
    private static final String HINT_PROVINSI = "Pilih Provinsi";
    private static final String HINT_KOTA = "Pilih Kabupaten / Kota";
    private static final String HINT_KECAMATAN = "Pilih Kecamatan";
    private static final String HINT_KODE_POS = "Pilih Kode Pos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Inisialisasi Firebase
        initFirebase();

        // Inisialisasi View
        initViews();

        // Setup tombol back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Load data wilayah
        loadWilayahData();

        // Setup Spinner
        setupSpinners();

        // Setup listener untuk spinner
        setupSpinnerListeners();

        //  Muat alamat terakhir dari Firestore
        loadLastAddressFromFirestore();

        // Ambil Nama Lengkap dari Firestore
        loadUserData();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup tombol simpan
        if (btnSimpan != null) {
            btnSimpan.setOnClickListener(v -> saveAddress());
        }

        // SETUP TOMBOL BUAT PESANAN
        btnBuatPesanan.setOnClickListener(v -> {
            if (validateOrder()) {
                saveOrderAndGoToRiwayat();
            }
        });

        // SETUP SCROLL LISTENER UNTUK total_container
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        LinearLayout layoutTotalTagihan = findViewById(R.id.layout_total_tagihan);

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int maxScroll = v.getChildAt(0).getHeight() - v.getHeight();
            if (scrollY >= maxScroll - 10) {
                layoutTotalTagihan.setVisibility(View.GONE);
            } else {
                layoutTotalTagihan.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            namaUser = user.getEmail();
            userId = user.getUid();
        } else {
            Toast.makeText(this, "User tidak login", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        edtJalan = findViewById(R.id.edtJalan);
        edtDetail = findViewById(R.id.edtDetail);
        btnSimpan = findViewById(R.id.btn_simpan);
        btnBuatPesanan = findViewById(R.id.btnBuatPesanan);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recycler_view_cart);
        radioGroupPembayaran = findViewById(R.id.radioGroupPembayaran);
        rbCOD = findViewById(R.id.rbCOD);
        rbBCA = findViewById(R.id.rbBCA);
        rbMandiri = findViewById(R.id.rbMandiri);
        rbBRI = findViewById(R.id.rbBRI);
        rbBNI = findViewById(R.id.rbBNI);

        // Spinner
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi);
        spinnerKota = findViewById(R.id.spinnerKota);
        spinnerKecamatan = findViewById(R.id.spinnerKecamatan);
        spinnerKodePos = findViewById(R.id.spinnerKodePos);
    }

    // Wilayah Kabupaten Bekasi dan Kota Bekasi
    private void loadWilayahData() {
        // Provinsi
        provinsiList.add(HINT_PROVINSI);
        provinsiList.add("JAWA BARAT");

        // Kota
        kotaList.add(HINT_KOTA);
        kotaList.add("KAB. BEKASI");
        kotaList.add("KOTA BEKASI");

        // Kecamatan di Kabupaten Bekasi
        List<String> kecamatanKabBekasi = new ArrayList<>();
        kecamatanKabBekasi.add(HINT_KECAMATAN);
        kecamatanKabBekasi.add("BABELAN");
        kecamatanKabBekasi.add("BOJONGMANGU");
        kecamatanKabBekasi.add("CABANGBUNGIN");
        kecamatanKabBekasi.add("CIBARUSAH");
        kecamatanKabBekasi.add("CIBITUNG");
        kecamatanKabBekasi.add("CIKARANG BARAT");
        kecamatanKabBekasi.add("CIKARANG PUSAT");
        kecamatanKabBekasi.add("CIKARANG SELATAN");
        kecamatanKabBekasi.add("CIKARANG TIMUR");
        kecamatanKabBekasi.add("CIKARANG UTARA");
        kecamatanKabBekasi.add("KARANGBAHAGIA");
        kecamatanKabBekasi.add("KEDUNG WARINGIN");
        kecamatanKabBekasi.add("MUARA GEMBONG");
        kecamatanKabBekasi.add("PEBAYURAN");
        kecamatanKabBekasi.add("SERANG BARU");
        kecamatanKabBekasi.add("SETU");
        kecamatanKabBekasi.add("SUKAKARYA");
        kecamatanKabBekasi.add("SUKATANI");
        kecamatanKabBekasi.add("SUKAWANGI");
        kecamatanKabBekasi.add("TAMBELANG");
        kecamatanKabBekasi.add("TAMBUN SELATAN");
        kecamatanKabBekasi.add("TAMBUN UTARA");
        kecamatanKabBekasi.add("TARUMAJAYA");

        // Kecamatan di Kota Bekasi
        List<String> kecamatanKotaBekasi = new ArrayList<>();
        kecamatanKotaBekasi.add(HINT_KECAMATAN);
        kecamatanKotaBekasi.add("BANTAR GEBANG");
        kecamatanKotaBekasi.add("BEKASI BARAT");
        kecamatanKotaBekasi.add("BEKASI SELATAN");
        kecamatanKotaBekasi.add("BEKASI TIMUR");
        kecamatanKotaBekasi.add("BEKASI UTARA");
        kecamatanKotaBekasi.add("JATISAMPURNA");
        kecamatanKotaBekasi.add("JATIASIH");
        kecamatanKotaBekasi.add("MEDAN SATRIA");
        kecamatanKotaBekasi.add("MUSTIKA JAYA");
        kecamatanKotaBekasi.add("PONDOK GEDE");
        kecamatanKotaBekasi.add("PONDOK MELATI");
        kecamatanKotaBekasi.add("RAWALUMBU");

        // Semua kode pos (dengan hint)
        List<String> kodePosBabelan = new ArrayList<>();
        kodePosBabelan.add(HINT_KODE_POS);
        kodePosBabelan.add("17610"); kodePosBabelan.add("17611"); kodePosBabelan.add("17612"); kodePosBabelan.add("17613"); kodePosBabelan.add("17614");
        kodePosBabelan.add("17615"); kodePosBabelan.add("17616"); kodePosBabelan.add("17617"); kodePosBabelan.add("17618"); kodePosBabelan.add("17619");

        List<String> kodePosBojongmangu = new ArrayList<>();
        kodePosBojongmangu.add(HINT_KODE_POS);
        kodePosBojongmangu.add("17350"); kodePosBojongmangu.add("17356"); kodePosBojongmangu.add("17451"); kodePosBojongmangu.add("17452");
        kodePosBojongmangu.add("17453"); kodePosBojongmangu.add("17455"); kodePosBojongmangu.add("17456"); kodePosBojongmangu.add("17457"); kodePosBojongmangu.add("17610");

        List<String> kodePosCabangbungin = new ArrayList<>();
        kodePosCabangbungin.add(HINT_KODE_POS);
        kodePosCabangbungin.add("17720"); kodePosCabangbungin.add("17721"); kodePosCabangbungin.add("17722"); kodePosCabangbungin.add("17723");
        kodePosCabangbungin.add("17724"); kodePosCabangbungin.add("17725"); kodePosCabangbungin.add("17726"); kodePosCabangbungin.add("17727"); kodePosCabangbungin.add("17728");

        List<String> kodePosCibarusah = new ArrayList<>();
        kodePosCibarusah.add(HINT_KODE_POS);
        kodePosCibarusah.add("17340"); kodePosCibarusah.add("17341"); kodePosCibarusah.add("17342"); kodePosCibarusah.add("17343"); kodePosCibarusah.add("17344");
        kodePosCibarusah.add("17345"); kodePosCibarusah.add("17346"); kodePosCibarusah.add("17347"); kodePosCibarusah.add("17720");

        List<String> kodePosCibitung = new ArrayList<>();
        kodePosCibitung.add(HINT_KODE_POS);
        kodePosCibitung.add("17340"); kodePosCibitung.add("17520"); kodePosCibitung.add("17521"); kodePosCibitung.add("17522"); kodePosCibitung.add("17523");
        kodePosCibitung.add("17524"); kodePosCibitung.add("17525"); kodePosCibitung.add("17526"); kodePosCibitung.add("17527");

        List<String> kodePosCikarangBarat = new ArrayList<>();
        kodePosCikarangBarat.add(HINT_KODE_POS);
        kodePosCikarangBarat.add("17841"); kodePosCikarangBarat.add("17842"); kodePosCikarangBarat.add("17843"); kodePosCikarangBarat.add("17844");
        kodePosCikarangBarat.add("17845"); kodePosCikarangBarat.add("17846"); kodePosCikarangBarat.add("17847"); kodePosCikarangBarat.add("17848"); kodePosCikarangBarat.add("17849");

        List<String> kodePosCikarangPusat = new ArrayList<>();
        kodePosCikarangPusat.add(HINT_KODE_POS);
        kodePosCikarangPusat.add("17811"); kodePosCikarangPusat.add("17812"); kodePosCikarangPusat.add("17813"); kodePosCikarangPusat.add("17814");
        kodePosCikarangPusat.add("17815"); kodePosCikarangPusat.add("17816");

        List<String> kodePosCikarangSelatan = new ArrayList<>();
        kodePosCikarangSelatan.add(HINT_KODE_POS);
        kodePosCikarangSelatan.add("17532"); kodePosCikarangSelatan.add("17851"); kodePosCikarangSelatan.add("17852"); kodePosCikarangSelatan.add("17853");
        kodePosCikarangSelatan.add("17854"); kodePosCikarangSelatan.add("17855"); kodePosCikarangSelatan.add("17856"); kodePosCikarangSelatan.add("17857");

        List<String> kodePosCikarangTimur = new ArrayList<>();
        kodePosCikarangTimur.add(HINT_KODE_POS);
        kodePosCikarangTimur.add("17821"); kodePosCikarangTimur.add("17822"); kodePosCikarangTimur.add("17823"); kodePosCikarangTimur.add("17824");
        kodePosCikarangTimur.add("17825"); kodePosCikarangTimur.add("17826"); kodePosCikarangTimur.add("17827"); kodePosCikarangTimur.add("17828");

        List<String> kodePosCikarangUtara = new ArrayList<>();
        kodePosCikarangUtara.add(HINT_KODE_POS);
        kodePosCikarangUtara.add("17831"); kodePosCikarangUtara.add("17832"); kodePosCikarangUtara.add("17833"); kodePosCikarangUtara.add("17834");
        kodePosCikarangUtara.add("17835"); kodePosCikarangUtara.add("17836"); kodePosCikarangUtara.add("17837"); kodePosCikarangUtara.add("17838"); kodePosCikarangUtara.add("17839");

        List<String> kodePosKarangbahagia = new ArrayList<>();
        kodePosKarangbahagia.add(HINT_KODE_POS);
        kodePosKarangbahagia.add("17530"); kodePosKarangbahagia.add("17531"); kodePosKarangbahagia.add("17532"); kodePosKarangbahagia.add("17533");
        kodePosKarangbahagia.add("17534"); kodePosKarangbahagia.add("17535"); kodePosKarangbahagia.add("17536"); kodePosKarangbahagia.add("17537");
        kodePosKarangbahagia.add("17538"); kodePosKarangbahagia.add("17620");

        List<String> kodePosKedungWaringin = new ArrayList<>();
        kodePosKedungWaringin.add(HINT_KODE_POS);
        kodePosKedungWaringin.add("17520"); kodePosKedungWaringin.add("17540"); kodePosKedungWaringin.add("17541"); kodePosKedungWaringin.add("17542");
        kodePosKedungWaringin.add("17543"); kodePosKedungWaringin.add("17544"); kodePosKedungWaringin.add("17545"); kodePosKedungWaringin.add("17546");
        kodePosKedungWaringin.add("17547"); kodePosKedungWaringin.add("17620");

        List<String> kodePosMuaraGembong = new ArrayList<>();
        kodePosMuaraGembong.add(HINT_KODE_POS);
        kodePosMuaraGembong.add("17540"); kodePosMuaraGembong.add("17730"); kodePosMuaraGembong.add("17731"); kodePosMuaraGembong.add("17732");
        kodePosMuaraGembong.add("17733"); kodePosMuaraGembong.add("17734"); kodePosMuaraGembong.add("17735"); kodePosMuaraGembong.add("17736");
        kodePosMuaraGembong.add("17547"); kodePosMuaraGembong.add("17620");

        List<String> kodePosPebayuran = new ArrayList<>();
        kodePosPebayuran.add(HINT_KODE_POS);
        kodePosPebayuran.add("17710"); kodePosPebayuran.add("17711"); kodePosPebayuran.add("17712"); kodePosPebayuran.add("17713"); kodePosPebayuran.add("17714");
        kodePosPebayuran.add("17715"); kodePosPebayuran.add("17716"); kodePosPebayuran.add("17717"); kodePosPebayuran.add("17718"); kodePosPebayuran.add("17719");

        List<String> kodePosSerangBaru = new ArrayList<>();
        kodePosSerangBaru.add(HINT_KODE_POS);
        kodePosSerangBaru.add("17330"); kodePosSerangBaru.add("17331"); kodePosSerangBaru.add("17332"); kodePosSerangBaru.add("17333"); kodePosSerangBaru.add("17334");
        kodePosSerangBaru.add("17335"); kodePosSerangBaru.add("17336"); kodePosSerangBaru.add("17337"); kodePosSerangBaru.add("17338"); kodePosSerangBaru.add("17710");

        List<String> kodePosSetu = new ArrayList<>();
        kodePosSetu.add(HINT_KODE_POS);
        kodePosSetu.add("17320"); kodePosSetu.add("17321"); kodePosSetu.add("17322"); kodePosSetu.add("17323"); kodePosSetu.add("17324");
        kodePosSetu.add("17325"); kodePosSetu.add("17326"); kodePosSetu.add("17327"); kodePosSetu.add("17328"); kodePosSetu.add("17329"); kodePosSetu.add("17330");

        List<String> kodePosSukakarya = new ArrayList<>();
        kodePosSukakarya.add(HINT_KODE_POS);
        kodePosSukakarya.add("17630"); kodePosSukakarya.add("17641"); kodePosSukakarya.add("17642"); kodePosSukakarya.add("17643");
        kodePosSukakarya.add("17644"); kodePosSukakarya.add("17645"); kodePosSukakarya.add("17646"); kodePosSukakarya.add("17647");

        List<String> kodePosSukatani = new ArrayList<>();
        kodePosSukatani.add(HINT_KODE_POS);
        kodePosSukatani.add("17320"); kodePosSukatani.add("17630"); kodePosSukatani.add("17631"); kodePosSukatani.add("17632"); kodePosSukatani.add("17633");
        kodePosSukatani.add("17634"); kodePosSukatani.add("17635"); kodePosSukatani.add("17636"); kodePosSukatani.add("17637");

        List<String> kodePosSukawangi = new ArrayList<>();
        kodePosSukawangi.add(HINT_KODE_POS);
        kodePosSukawangi.add("17620"); kodePosSukawangi.add("17630"); kodePosSukawangi.add("17651"); kodePosSukawangi.add("17652");
        kodePosSukawangi.add("17653"); kodePosSukawangi.add("17654"); kodePosSukawangi.add("17655"); kodePosSukawangi.add("17656"); kodePosSukawangi.add("17657");

        List<String> kodePosTambelang = new ArrayList<>();
        kodePosTambelang.add(HINT_KODE_POS);
        kodePosTambelang.add("17620"); kodePosTambelang.add("17621"); kodePosTambelang.add("17622"); kodePosTambelang.add("17623"); kodePosTambelang.add("17624");
        kodePosTambelang.add("17625"); kodePosTambelang.add("17626"); kodePosTambelang.add("17627"); kodePosTambelang.add("17630");

        List<String> kodePosTambunSelatan = new ArrayList<>();
        kodePosTambunSelatan.add(HINT_KODE_POS);
        kodePosTambunSelatan.add("17510"); kodePosTambunSelatan.add("17511"); kodePosTambunSelatan.add("17512"); kodePosTambunSelatan.add("17513");
        kodePosTambunSelatan.add("17514"); kodePosTambunSelatan.add("17515"); kodePosTambunSelatan.add("17516"); kodePosTambunSelatan.add("17517");
        kodePosTambunSelatan.add("17518"); kodePosTambunSelatan.add("17519"); kodePosTambunSelatan.add("17620");

        List<String> kodePosTambunUtara = new ArrayList<>();
        kodePosTambunUtara.add(HINT_KODE_POS);
        kodePosTambunUtara.add("17510"); kodePosTambunUtara.add("17561"); kodePosTambunUtara.add("17562"); kodePosTambunUtara.add("17563");
        kodePosTambunUtara.add("17564"); kodePosTambunUtara.add("17565"); kodePosTambunUtara.add("17566"); kodePosTambunUtara.add("17567"); kodePosTambunUtara.add("17568");

        List<String> kodePosTarumajaya = new ArrayList<>();
        kodePosTarumajaya.add(HINT_KODE_POS);
        kodePosTarumajaya.add("17211"); kodePosTarumajaya.add("17212"); kodePosTarumajaya.add("17213"); kodePosTarumajaya.add("17214");
        kodePosTarumajaya.add("17215"); kodePosTarumajaya.add("17216"); kodePosTarumajaya.add("17217"); kodePosTarumajaya.add("17218"); kodePosTarumajaya.add("17510");

        List<String> kodePosBantarGebang = new ArrayList<>();
        kodePosBantarGebang.add(HINT_KODE_POS);
        kodePosBantarGebang.add("17151"); kodePosBantarGebang.add("17152"); kodePosBantarGebang.add("17153"); kodePosBantarGebang.add("17154");
        kodePosBantarGebang.add("17215"); kodePosBantarGebang.add("17216"); kodePosBantarGebang.add("17217"); kodePosBantarGebang.add("17218"); kodePosBantarGebang.add("17510");

        List<String> kodePosBekasiBarat = new ArrayList<>();
        kodePosBekasiBarat.add(HINT_KODE_POS);
        kodePosBekasiBarat.add("17133"); kodePosBekasiBarat.add("17134"); kodePosBekasiBarat.add("17135"); kodePosBekasiBarat.add("17136");
        kodePosBekasiBarat.add("17137"); kodePosBekasiBarat.add("17139"); kodePosBekasiBarat.add("17145");

        List<String> kodePosBekasiSelatan = new ArrayList<>();
        kodePosBekasiSelatan.add(HINT_KODE_POS);
        kodePosBekasiSelatan.add("17141"); kodePosBekasiSelatan.add("17144"); kodePosBekasiSelatan.add("17146"); kodePosBekasiSelatan.add("17147"); kodePosBekasiSelatan.add("17148");

        List<String> kodePosBekasiTimur = new ArrayList<>();
        kodePosBekasiTimur.add(HINT_KODE_POS);
        kodePosBekasiTimur.add("17111"); kodePosBekasiTimur.add("17112"); kodePosBekasiTimur.add("17113"); kodePosBekasiTimur.add("17118");

        List<String> kodePosBekasiUtara = new ArrayList<>();
        kodePosBekasiUtara.add(HINT_KODE_POS);
        kodePosBekasiUtara.add("17121"); kodePosBekasiUtara.add("17122"); kodePosBekasiUtara.add("17123"); kodePosBekasiUtara.add("17124");
        kodePosBekasiUtara.add("17125"); kodePosBekasiUtara.add("17126"); kodePosBekasiUtara.add("17127");

        List<String> kodePosJatisampurna = new ArrayList<>();
        kodePosJatisampurna.add(HINT_KODE_POS);
        kodePosJatisampurna.add("17432"); kodePosJatisampurna.add("17433"); kodePosJatisampurna.add("17434"); kodePosJatisampurna.add("17435"); kodePosJatisampurna.add("17436");

        List<String> kodePosJatiasih = new ArrayList<>();
        kodePosJatiasih.add(HINT_KODE_POS);
        kodePosJatiasih.add("17421"); kodePosJatiasih.add("17422"); kodePosJatiasih.add("17423"); kodePosJatiasih.add("17424");
        kodePosJatiasih.add("17425"); kodePosJatiasih.add("17426");

        List<String> kodePosMedanSatria = new ArrayList<>();
        kodePosMedanSatria.add(HINT_KODE_POS);
        kodePosMedanSatria.add("17131"); kodePosMedanSatria.add("17132"); kodePosMedanSatria.add("17133"); kodePosMedanSatria.add("17143");
        kodePosMedanSatria.add("17181"); kodePosMedanSatria.add("17182"); kodePosMedanSatria.add("17183"); kodePosMedanSatria.add("17184");

        List<String> kodePosMustikaJaya = new ArrayList<>();
        kodePosMustikaJaya.add(HINT_KODE_POS);
        kodePosMustikaJaya.add("17156"); kodePosMustikaJaya.add("17157"); kodePosMustikaJaya.add("17158"); kodePosMustikaJaya.add("17165");
        kodePosMustikaJaya.add("17166"); kodePosMustikaJaya.add("17167"); kodePosMustikaJaya.add("17168");

        List<String> kodePosPondokGede = new ArrayList<>();
        kodePosPondokGede.add(HINT_KODE_POS);
        kodePosPondokGede.add("17155"); kodePosPondokGede.add("17411"); kodePosPondokGede.add("17412"); kodePosPondokGede.add("17413");
        kodePosPondokGede.add("17416"); kodePosPondokGede.add("17417");

        List<String> kodePosPondokMelati = new ArrayList<>();
        kodePosPondokMelati.add(HINT_KODE_POS);
        kodePosPondokMelati.add("17414"); kodePosPondokMelati.add("17415"); kodePosPondokMelati.add("17431"); kodePosPondokMelati.add("17441");
        kodePosPondokMelati.add("17444"); kodePosPondokMelati.add("17445"); kodePosPondokMelati.add("17446");

        List<String> kodePosRawalumbu = new ArrayList<>();
        kodePosRawalumbu.add(HINT_KODE_POS);
        kodePosRawalumbu.add("17114"); kodePosRawalumbu.add("17115"); kodePosRawalumbu.add("17116"); kodePosRawalumbu.add("17117");
        kodePosRawalumbu.add("17174"); kodePosRawalumbu.add("17175"); kodePosRawalumbu.add("17176"); kodePosRawalumbu.add("17177"); kodePosRawalumbu.add("17414");

        // Mapping data
        kotaToKecamatan.put("KAB. BEKASI", kecamatanKabBekasi);
        kotaToKecamatan.put("KOTA BEKASI", kecamatanKotaBekasi);

        // Kecamatan di Kabupaten Bekasi
        kecamatanToKodePos.put("BABELAN", kodePosBabelan);
        kecamatanToKodePos.put("BOJONGMANGU", kodePosBojongmangu);
        kecamatanToKodePos.put("CIBARUSAH", kodePosCibarusah);
        kecamatanToKodePos.put("CIBITUNG", kodePosCibitung);
        kecamatanToKodePos.put("CIKARANG BARAT", kodePosCikarangBarat);
        kecamatanToKodePos.put("CIKARANG PUSAT", kodePosCikarangPusat);
        kecamatanToKodePos.put("CIKARANG SELATAN", kodePosCikarangSelatan);
        kecamatanToKodePos.put("CIKARANG TIMUR", kodePosCikarangTimur);
        kecamatanToKodePos.put("KARANGBAHAGIA", kodePosKarangbahagia);
        kecamatanToKodePos.put("KEDUNG WARINGIN", kodePosKedungWaringin);
        kecamatanToKodePos.put("MUARA GEMBONG", kodePosMuaraGembong);
        kecamatanToKodePos.put("PEBAYURAN", kodePosPebayuran);
        kecamatanToKodePos.put("SERANG BARU", kodePosSerangBaru);
        kecamatanToKodePos.put("SETU", kodePosSetu);
        kecamatanToKodePos.put("SUKAKARYA", kodePosSukakarya);
        kecamatanToKodePos.put("SUKATANI", kodePosSukatani);
        kecamatanToKodePos.put("SUKAWANGI", kodePosSukawangi);
        kecamatanToKodePos.put("TAMBELANG", kodePosTambelang);
        kecamatanToKodePos.put("TAMBUN SELATAN", kodePosTambunSelatan);
        kecamatanToKodePos.put("TAMBUN UTARA", kodePosTambunUtara);
        kecamatanToKodePos.put("TARUMAJAYA", kodePosTarumajaya);

        // Kecamatan di Kota Bekasi
        kecamatanToKodePos.put("BANTAR GEBANG", kodePosBantarGebang);
        kecamatanToKodePos.put("BEKASI BARAT", kodePosBekasiBarat);
        kecamatanToKodePos.put("BEKASI SELATAN", kodePosBekasiSelatan);
        kecamatanToKodePos.put("BEKASI TIMUR", kodePosBekasiTimur);
        kecamatanToKodePos.put("BEKASI UTARA", kodePosBekasiUtara);
        kecamatanToKodePos.put("JATISAMPURNA", kodePosJatisampurna);
        kecamatanToKodePos.put("JATIASIH", kodePosJatiasih);
        kecamatanToKodePos.put("MEDAN SATRIA", kodePosMedanSatria);
        kecamatanToKodePos.put("MUSTIKA JAYA", kodePosMustikaJaya);
        kecamatanToKodePos.put("PONDOK GEDE", kodePosPondokGede);
        kecamatanToKodePos.put("PONDOK MELATI", kodePosPondokMelati);
        kecamatanToKodePos.put("RAWALUMBU", kodePosRawalumbu);
    }

    private void setupSpinners() {
        // Setup Provinsi
        ArrayAdapter<String> adapterProvinsi = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinsiList);
        adapterProvinsi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvinsi.setAdapter(adapterProvinsi);
        spinnerProvinsi.setSelection(0); // Hint

        // Setup Kota
        ArrayAdapter<String> adapterKota = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kotaList);
        adapterKota.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKota.setAdapter(adapterKota);
        spinnerKota.setSelection(0); // Hint

        // Setup Kecamatan
        ArrayAdapter<String> adapterKecamatan = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kecamatanList);
        adapterKecamatan.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKecamatan.setAdapter(adapterKecamatan);
        spinnerKecamatan.setSelection(0); // Hint

        // Setup Kode Pos
        ArrayAdapter<String> adapterKodePos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kodePosList);
        adapterKodePos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKodePos.setAdapter(adapterKodePos);
        spinnerKodePos.setSelection(0); // Hint
    }

    private void setupSpinnerListeners() {
        spinnerKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String kota = parent.getItemAtPosition(position).toString();

                if (kota.equals(HINT_KOTA)) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this,
                            android.R.layout.simple_spinner_item, new ArrayList<String>() {{
                        add(HINT_KECAMATAN);
                    }});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKecamatan.setAdapter(adapter);
                    spinnerKecamatan.setSelection(0);
                    return;
                }

                List<String> kecamatan = kotaToKecamatan.get(kota);
                if (kecamatan != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this,
                            android.R.layout.simple_spinner_item, kecamatan);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKecamatan.setAdapter(adapter);
                    spinnerKecamatan.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerKecamatan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String kecamatan = parent.getItemAtPosition(position).toString();

                if (kecamatan.equals(HINT_KECAMATAN)) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this,
                            android.R.layout.simple_spinner_item, new ArrayList<String>() {{
                        add(HINT_KODE_POS);
                    }});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKodePos.setAdapter(adapter);
                    spinnerKodePos.setSelection(0);
                    return;
                }

                List<String> kodePos = kecamatanToKodePos.get(kecamatan);
                if (kodePos != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CheckoutActivity.this,
                            android.R.layout.simple_spinner_item, kodePos);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKodePos.setAdapter(adapter);
                    spinnerKodePos.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUserData() {
        if (userId == null) return;

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nama = documentSnapshot.getString("NamaLengkap");
                        if (nama != null && !nama.isEmpty()) {
                            this.namaLengkap = nama; // Simpan ke variabel global
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Checkout", "Gagal ambil data user", e);
                    // Tetap pakai "Pelanggan"
                });
    }

    private void setupRecyclerView() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("cart_items");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        cartItemList = gson.fromJson(json, type);

        if (cartItemList == null) {
            cartItemList = new ArrayList<>();
        }

        cartAdapter = new CartAdapter(cartItemList, new CartAdapter.OnItemClickListener() {
            @Override public void onItemClick(int position) {}
            @Override public void onAddQuantity(int position) {
                CartItem item = cartItemList.get(position);
                item.setJumlah(item.getJumlah() + 1);
                cartAdapter.notifyItemChanged(position);
                updateTotal();
            }
            @Override public void onSubtractQuantity(int position) {
                CartItem item = cartItemList.get(position);
                if (item.getJumlah() > 1) {
                    item.setJumlah(item.getJumlah() - 1);
                    cartAdapter.notifyItemChanged(position);
                    updateTotal();
                }
            }
            @Override public void onRemoveItem(int position) {
                cartItemList.remove(position);
                cartAdapter.notifyItemRemoved(position);
                updateTotal();
            }
        });

        if (recyclerView != null) {
            recyclerView.setAdapter(cartAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            updateTotal();
        } else {
            Toast.makeText(this, "❌ RecyclerView tidak ditemukan di layout", Toast.LENGTH_LONG).show();
        }
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (value != null && adapter != null) {
            int pos = adapter.getPosition(value);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    private void loadLastAddressFromFirestore() {
        firestore.collection("users")
                .document(userId)
                .collection("alamat")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String provinsi = doc.getString("provinsi");
                        String kota = doc.getString("kota");
                        String kecamatan = doc.getString("kecamatan");
                        String kodePos = doc.getString("kodePos");
                        String jalan = doc.getString("jalan");
                        String detail = doc.getString("detail");

                        // Set ke Spinner
                        setSpinnerToValue(spinnerProvinsi, provinsi);
                        setSpinnerToValue(spinnerKota, kota);

                        // Tunggu filter kecamatan
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            setSpinnerToValue(spinnerKecamatan, kecamatan);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                setSpinnerToValue(spinnerKodePos, kodePos);
                            }, 100);
                        }, 100);

                        edtJalan.setText(jalan);
                        edtDetail.setText(detail);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Checkout", "Gagal muat alamat dari Firestore", e);
                });
    }

    private void saveAddress() {
        String provinsi = spinnerProvinsi.getSelectedItem().toString();
        String kota = spinnerKota.getSelectedItem().toString();
        String kecamatan = spinnerKecamatan.getSelectedItem().toString();
        String kodePos = spinnerKodePos.getSelectedItem().toString();
        String jalan = edtJalan.getText().toString().trim();
        String detail = edtDetail.getText().toString().trim();

        if (jalan.isEmpty()) {
            edtJalan.setError("Nama jalan tidak boleh kosong");
            edtJalan.requestFocus();
            return;
        }

        //  Simpan ke SharedPreferences (untuk lokal)
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("saved_provinsi_" + userId, provinsi);
        editor.putString("saved_kota_" + userId, kota);
        editor.putString("saved_kecamatan_" + userId, kecamatan);
        editor.putString("saved_kodepos_" + userId, kodePos);
        editor.putString("saved_jalan_" + userId, jalan);
        editor.putString("saved_detail_" + userId, detail);
        editor.apply();

        //  Simpan ke Firestore (untuk permanen & multi-perangkat)
        Map<String, Object> alamat = new HashMap<>();
        alamat.put("provinsi", provinsi);
        alamat.put("kota", kota);
        alamat.put("kecamatan", kecamatan);
        alamat.put("kodePos", kodePos);
        alamat.put("jalan", jalan);
        alamat.put("detail", detail);
        alamat.put("timestamp", System.currentTimeMillis());

        firestore.collection("users")
                .document(userId)
                .collection("alamat")
                .add(alamat)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Alamat berhasil disimpan", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menyimpan alamat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateOrder() {
        if (cartItemList == null || cartItemList.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show();
            return false;
        }

        String alamat = edtJalan.getText().toString().trim();
        if (alamat.isEmpty()) {
            edtJalan.setError("Alamat tidak boleh kosong");
            edtJalan.requestFocus();
            return false;
        }

        // Validasi Wilayah
        if (!isInAllowedArea()) {
            Toast.makeText(this, "Pesanan hanya dapat diproses untuk seluruh wilayah Kota dan Kabupaten Bekasi", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validasi Metode Pembayaran
        if (!rbCOD.isChecked() && !rbBCA.isChecked() && !rbMandiri.isChecked() &&
                !rbBRI.isChecked() && !rbBNI.isChecked()) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (namaLengkap == null) {
            Toast.makeText(this, "Memuat data user...", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isInAllowedArea() {
        String provinsi = spinnerProvinsi.getSelectedItem().toString();
        String kota = spinnerKota.getSelectedItem().toString();
        return provinsi.equals("JAWA BARAT") &&
                (kota.equals("KAB. BEKASI") || kota.equals("KOTA BEKASI"));
    }

    private void saveOrderAndGoToRiwayat() {
        // Ambil metode pembayaran
        String metodePembayaran;
        if (rbCOD.isChecked()) {
            metodePembayaran = "COD (Bayar di Tempat)";
        } else if (rbBCA.isChecked()) {
            metodePembayaran = "BCA Virtual Account";
        } else if (rbMandiri.isChecked()) {
            metodePembayaran = "Mandiri Virtual Account";
        } else if (rbBRI.isChecked()) {
            metodePembayaran = "BRI Virtual Account";
        } else if (rbBNI.isChecked()) {
            metodePembayaran = "BNI Virtual Account";
        } else {
            metodePembayaran = "Metode tidak diketahui";
        }

        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (CartItem item : cartItemList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("namaPesanan", item.getNamaMenu());
            itemMap.put("jumlah", item.getJumlah());
            itemMap.put("totalPesanan", (long) (item.getHarga() * item.getJumlah()));
            itemMap.put("imageUrl", item.getImageUrl());
            orderItems.add(itemMap);
        }

        // Buat data pesanan
        if (namaLengkap == null || namaLengkap.isEmpty()) {
            Toast.makeText(this, "Memuat nama user...", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Gabungkan semua data alamat
        String provinsi = spinnerProvinsi.getSelectedItem().toString();
        String kota = spinnerKota.getSelectedItem().toString();
        String kecamatan = spinnerKecamatan.getSelectedItem().toString();
        String kodePos = spinnerKodePos.getSelectedItem().toString();
        String jalan = edtJalan.getText().toString().trim();
        String detail = edtDetail.getText().toString().trim();

        //  Format alamat lengkap
        String alamatLengkap = jalan + ", " + detail + "\n" +
                kecamatan + ", " + kota + "\n" +
                provinsi + " " + kodePos;

        Map<String, Object> order = new HashMap<>();
        order.put("NamaLengkap", namaLengkap);
        order.put("Alamat", alamatLengkap);
        order.put("pesanan", orderItems);
        order.put("Ongkir", (long) ongkir);
        order.put("totalHarga", (long) calculateTotal());
        order.put("Email", namaUser);
        order.put("metodePembayaran", metodePembayaran);
        order.put("timestamp", System.currentTimeMillis());

        // Status Default Untuk User Cod dan Transfer
        if ("COD (Bayar di Tempat)".equals(metodePembayaran)) {
            order.put("status", "Menunggu Konfirmasi");
        } else {
            order.put("status", "Menunggu Pembayaran");
        }

        // Format tanggal
        String tanggal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date());
        order.put("tanggal", tanggal);

        // Simpan deadline: 15 menit dari sekarang
        long deadline = System.currentTimeMillis() + (1 * 60 * 1000);
        order.put("deadline", deadline);

        // Simpan ke Firestore
        firestore.collection("pesanan")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    clearCart();
                    Toast.makeText(this, "Pesanan berhasil dibuat", Toast.LENGTH_SHORT).show();

                    // Lanjut ke aktivitas berikutnya
                    final String finalMetodePembayaran = metodePembayaran;
                    if ("COD (Bayar di Tempat)".equals(finalMetodePembayaran)) {
                        // Langsung ke halaman Riwayat Pesanan
                        Intent intent = new Intent(CheckoutActivity.this, RiwayatPesananActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        // Ke Pembayaran
                        Intent intent = new Intent(CheckoutActivity.this, PembayaranActivity.class);
                        intent.putExtra("metode", finalMetodePembayaran);
                        intent.putExtra("total", (long) calculateTotal());
                        intent.putExtra("pesanan_id", documentReference.getId());
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal membuat pesanan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void onPaymentMethodClicked(View view) {
        rbCOD.setChecked(false);
        rbBCA.setChecked(false);
        rbMandiri.setChecked(false);
        rbBRI.setChecked(false);
        rbBNI.setChecked(false);

        LinearLayout parent = (LinearLayout) view;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof RadioButton) {
                ((RadioButton) child).setChecked(true);
                break;
            }
        }
    }

    private int calculateTotal() {
        int total = 0;
        for (CartItem item : cartItemList) {
            total += item.getHarga() * item.getJumlah();
        }
        return total + ongkir;
    }

    private void updateTotal() {
        int total = calculateTotal();
        int totalJumlahItem = 0;
        for (CartItem item : cartItemList) {
            totalJumlahItem += item.getJumlah();
        }

        String formattedTotal = "Rp " + rp(total);

        TextView txtTotalTagihan = findViewById(R.id.txtTotalTagihan);
        if (txtTotalTagihan != null) txtTotalTagihan.setText(formattedTotal);

        TextView txtTotalTagihanRingkasan = findViewById(R.id.txtTotalTagihan_Ringkasan);
        if (txtTotalTagihanRingkasan != null) txtTotalTagihanRingkasan.setText(formattedTotal);

        TextView txtTotalHargaBarang = findViewById(R.id.txtTotalHargaBarang);
        if (txtTotalHargaBarang != null) txtTotalHargaBarang.setText("Rp " + rp(total - ongkir));

        TextView txtLabelTotalHarga = findViewById(R.id.txtLabelTotalHarga);
        if (txtLabelTotalHarga != null) {
            txtLabelTotalHarga.setText("Total Harga (" + totalJumlahItem + " Barang)");
        }

        TextView txtOngkirView = findViewById(R.id.txtOngkir);
        if (txtOngkirView != null) txtOngkirView.setText("Rp " + rp(ongkir));
    }

    private void clearCart() {
        SharedPreferences sp = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("cart items_" + userId);
        editor.apply();
    }

    public static String rp(long amount) {
        Locale locale = new Locale("in", "ID");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setMaximumFractionDigits(0);
        String formattedAmount = format.format(amount);
        return formattedAmount.replace("Rp", "").trim();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}