package com.suitexen.ecokeep.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.adapters.ScannedItemAdapter;
import com.suitexen.ecokeep.database.AppDatabase;
import com.suitexen.ecokeep.models.FoodItem;
import com.suitexen.ecokeep.models.GroqChatRequest;
import com.suitexen.ecokeep.models.GroqChatResponse;
import com.suitexen.ecokeep.models.ScannedItem;
import com.suitexen.ecokeep.network.RetrofitClient;
import com.suitexen.ecokeep.services.ReceiptScanner;
import com.suitexen.ecokeep.utils.Config;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanReceiptActivity extends AppCompatActivity {

    // Views — Loading
    private LinearLayout layoutLoading;
    private ProgressBar progressBar;
    private TextView tvLoadingText;

    // Views — Error
    private LinearLayout layoutError;
    private TextView tvErrorText;
    private MaterialButton btnRetry;

    // Views — Success
    private ImageView ivSuccessIcon;
    private TextView tvItemsFound;
    private TextView tvSubtitle;
    private View cardItems;
    private MaterialButton btnSaveAllItems;

    private RecyclerView rvScannedItems;
    private ScannedItemAdapter adapter;
    private List<ScannedItem> scannedItems = new ArrayList<>();

    private Uri currentImageUri;
    private String extractedRawText = "";

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    // ==================== SYSTEM PROMPT ====================
    private static final String SYSTEM_PROMPT =
            "You are an expert receipt parser AI for the EcoKeep app. " +
                    "Analyze the raw text scanned from a grocery receipt. " +
                    "Extract all valid food or grocery items. " +
                    "For each item, map it strictly to these rules:\n" +
                    "name: Clean item name (e.g., 'Organic Milk' instead of 'ORG MLK 1L').\n" +
                    "category: Must be EXACTLY one of these: [Vegetables, Fruits, Dairy, Meat, Frozen, Snacks, Drinks, Others]. If unsure, use 'Others'.\n" +
                    "totalQuantity: Extract number, default to 1.0 if not specified.\n" +
                    "unit: Must be EXACTLY one of: [pcs, kg, g, L, ml, pack]. Pick the closest match.\n" +
                    "price: Item total price as a double. Default to 0.0 if not found.\n\n" +
                    "Return response strictly in valid JSON format matching this schema:\n" +
                    "{ \"items\": [ { \"name\": \"...\", \"category\": \"...\", \"totalQuantity\": 1.0, \"unit\": \"...\", \"price\": 0.0 } ] }\n" +
                    "Do NOT include any markdown, backticks (```), or conversational text. Only pure JSON.";

    // ==================== CATEGORY EMOJI MAP ====================
    private static final Map<String, String> CATEGORY_EMOJI = new HashMap<>();

    static {
        CATEGORY_EMOJI.put("Vegetables", "🥦");
        CATEGORY_EMOJI.put("Fruits", "🍎");
        CATEGORY_EMOJI.put("Dairy", "🥛");
        CATEGORY_EMOJI.put("Meat", "🥩");
        CATEGORY_EMOJI.put("Frozen", "🧊");
        CATEGORY_EMOJI.put("Snacks", "🍿");
        CATEGORY_EMOJI.put("Drinks", "🥤");
        CATEGORY_EMOJI.put("Others", "🛒");
    }

    private static String getEmojiForCategory(String category) {
        return CATEGORY_EMOJI.getOrDefault(category, "🛒");
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupImagePicker();
        setupBackButton();

        // Check if image URI was passed via intent
        Uri imageUri = getIntent().getParcelableExtra("IMAGE_URI");
        if (imageUri != null) {
            currentImageUri = imageUri;
            startScanning(imageUri);
        } else {
            // No image provided — launch picker
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }

    // ==================== INIT ====================

    private void initViews() {
        // Loading
        layoutLoading = findViewById(R.id.layoutLoading);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingText = findViewById(R.id.tvLoadingText);

        // Error
        layoutError = findViewById(R.id.layoutError);
        tvErrorText = findViewById(R.id.tvErrorText);
        btnRetry = findViewById(R.id.btnRetry);

        // Success
        ivSuccessIcon = findViewById(R.id.ivSuccessIcon);
        tvItemsFound = findViewById(R.id.tvItemsFound);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        cardItems = findViewById(R.id.cardItems);
        rvScannedItems = findViewById(R.id.rvScannedItems);
        btnSaveAllItems = findViewById(R.id.btnSaveAllItems);

        // Retry button
        btnRetry.setOnClickListener(v -> {
            if (currentImageUri != null) {
                showLoadingState();
                startScanning(currentImageUri);
            } else {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        // Save button
        btnSaveAllItems.setOnClickListener(v -> saveAllItems());
    }

    private void setupImagePicker() {
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        currentImageUri = uri;
                        startScanning(uri);
                    } else {
                        // User cancelled picker
                        finish();
                    }
                }
        );
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    // ==================== STATE MANAGEMENT ====================

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        ivSuccessIcon.setVisibility(View.GONE);
        tvItemsFound.setVisibility(View.GONE);
        tvSubtitle.setVisibility(View.GONE);
        cardItems.setVisibility(View.GONE);
        btnSaveAllItems.setVisibility(View.GONE);
    }

    private void showLoadingMessage(String message) {
        tvLoadingText.setText(message);
    }

    private void showErrorState(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        ivSuccessIcon.setVisibility(View.GONE);
        tvItemsFound.setVisibility(View.GONE);
        tvSubtitle.setVisibility(View.GONE);
        cardItems.setVisibility(View.GONE);
        btnSaveAllItems.setVisibility(View.GONE);
        tvErrorText.setText(message);
    }

    private void showSuccessState() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        ivSuccessIcon.setVisibility(View.VISIBLE);
        tvItemsFound.setVisibility(View.VISIBLE);
        tvSubtitle.setVisibility(View.VISIBLE);
        cardItems.setVisibility(View.VISIBLE);
        btnSaveAllItems.setVisibility(View.VISIBLE);

        tvItemsFound.setText(String.format(Locale.getDefault(), "Items Found (%d)", scannedItems.size()));
    }

    // ==================== SCANNING FLOW ====================

    private void startScanning(Uri imageUri) {
        showLoadingState();
        showLoadingMessage("Reading text from receipt...");

        ReceiptScanner.scanReceipt(this, imageUri, new ReceiptScanner.ScanCallback() {
            @Override
            public void onSuccess(String extractedText) {
                extractedRawText = extractedText;
                runOnUiThread(() -> {
                    showLoadingMessage("Analyzing items with AI...");
                    processWithGroq(extractedText);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> showErrorState("Could not read text from image.\n" + e.getMessage()));
            }
        });
    }

    // ==================== GROQ API ====================

    private void processWithGroq(String rawText) {
        // Build messages
        List<GroqChatRequest.Message> messages = new ArrayList<>();
        messages.add(new GroqChatRequest.Message("system", SYSTEM_PROMPT));
        messages.add(new GroqChatRequest.Message("user", "Here is the raw receipt text:\n\n" + rawText));

        // Force JSON response
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");

        GroqChatRequest request = new GroqChatRequest(
                Config.GROQ_MODEL,
                messages,
                responseFormat,
                0.1  // Low temperature for deterministic output
        );

        String authHeader = "Bearer " + Config.GROQ_API_KEY;

        RetrofitClient.getGroqApiService().processReceipt(authHeader, request).enqueue(new Callback<GroqChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<GroqChatResponse> call, @NonNull Response<GroqChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseGroqResponse(response.body());
                } else {
                    String errorMsg = "AI processing failed";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception ignored) {}
                    }
                    showErrorState("AI Error: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroqChatResponse> call, @NonNull Throwable t) {
                showErrorState("Network error: " + t.getMessage());
            }
        });
    }

    private void parseGroqResponse(GroqChatResponse response) {
        try {
            if (response.getChoices() == null || response.getChoices().isEmpty()) {
                showErrorState("AI returned no results");
                return;
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            content = cleanJsonResponse(content);

            // Parse JSON
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType();
            Map<String, List<Map<String, Object>>> resultMap = gson.fromJson(content, mapType);

            if (resultMap == null || !resultMap.containsKey("items") || resultMap.get("items").isEmpty()) {
                showErrorState("No items could be identified from the receipt.");
                return;
            }

            // Map to ScannedItem objects
            scannedItems.clear();
            for (Map<String, Object> itemMap : resultMap.get("items")) {
                ScannedItem item = new ScannedItem();
                item.setName(getStringOr(itemMap, "name", "Unknown"));
                item.setCategory(getStringOr(itemMap, "category", "Others"));
                item.setTotalQuantity(getDoubleOr(itemMap, "totalQuantity", 1.0));
                item.setUnit(getStringOr(itemMap, "unit", "pcs"));
                item.setPrice(getDoubleOr(itemMap, "price", 0.0));
                item.setEmoji(getEmojiForCategory(item.getCategory()));
                scannedItems.add(item);
            }

            showSuccessState();
            setupRecyclerView();

        } catch (JsonSyntaxException e) {
            showErrorState("Failed to parse AI response. Please try again.");
        } catch (Exception e) {
            showErrorState("Unexpected error: " + e.getMessage());
        }
    }

    // ==================== JSON HELPERS ====================

    private String cleanJsonResponse(String content) {
        content = content.trim();
        // Strip markdown code blocks if present
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        return content.trim();
    }

    private String getStringOr(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private double getDoubleOr(Map<String, Object> map, String key, double defaultVal) {
        Object val = map.get(key);
        if (val == null) return defaultVal;
        try {
            if (val instanceof Number) return ((Number) val).doubleValue();
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    // ==================== RECYCLER VIEW ====================

    private void setupRecyclerView() {
        adapter = new ScannedItemAdapter(scannedItems);
        adapter.setOnEditClickListener(this::showEditBottomSheet);
        rvScannedItems.setLayoutManager(new LinearLayoutManager(this));
        rvScannedItems.setAdapter(adapter);
    }

    // ==================== EDIT BOTTOM SHEET ====================

    private void showEditBottomSheet(int position, ScannedItem item) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.layout_edit_scanned_item, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText etItemName = sheetView.findViewById(R.id.etItemName);
        Spinner spinnerCategory = sheetView.findViewById(R.id.spinnerCategory);
        EditText etQuantity = sheetView.findViewById(R.id.etQuantity);
        Spinner spinnerUnit = sheetView.findViewById(R.id.spinnerUnit);
        EditText etPrice = sheetView.findViewById(R.id.etPrice);
        MaterialButton btnSaveEdit = sheetView.findViewById(R.id.btnSaveEdit);

        // Populate fields
        etItemName.setText(item.getName());
        etQuantity.setText(String.valueOf(item.getTotalQuantity()));
        etPrice.setText(String.valueOf(item.getPrice()));

        // Setup Category Spinner
        String[] categories = {"Vegetables", "Fruits", "Dairy", "Meat", "Frozen", "Snacks", "Drinks", "Others"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Set selected category
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(item.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Setup Unit Spinner
        String[] units = {"pcs", "kg", "g", "L", "ml", "pack"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        // Set selected unit
        for (int i = 0; i < units.length; i++) {
            if (units[i].equalsIgnoreCase(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }

        // Save
        btnSaveEdit.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = spinnerCategory.getSelectedItem().toString();
            String unit = spinnerUnit.getSelectedItem().toString();
            double quantity = quantityStr.isEmpty() ? 1.0 : Double.parseDouble(quantityStr);
            double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);

            // Update item
            item.setName(name);
            item.setCategory(category);
            item.setTotalQuantity(quantity);
            item.setUnit(unit);
            item.setPrice(price);
            item.setEmoji(getEmojiForCategory(category));

            // Update adapter
            adapter.updateItem(position, item);

            // Update count title in case nothing changes, just refresh
            tvItemsFound.setText(String.format(Locale.getDefault(), "Items Found (%d)", scannedItems.size()));

            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    // ==================== SAVE ALL TO ROOM ====================

    private void saveAllItems() {
        if (scannedItems.isEmpty()) {
            Toast.makeText(this, "No items to save", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);

        for (ScannedItem scannedItem : scannedItems) {
            FoodItem foodItem = new FoodItem(
                    scannedItem.getName(),
                    scannedItem.getCategory(),
                    scannedItem.getTotalQuantity(),
                    scannedItem.getUnit(),
                    getSmartExpiryDate(scannedItem.getCategory()),
                    "",                         // no image from receipt scan
                    "",                         // no notes
                    true,                       // reminder enabled by default
                    scannedItem.getPrice(),
                    System.currentTimeMillis()
            );
            db.foodDao().insert(foodItem);
        }

        Toast.makeText(this, String.format(Locale.getDefault(), "%d items saved to inventory!", scannedItems.size()), Toast.LENGTH_SHORT).show();
        finish();
    }

    // ==================== SMART EXPIRY DATE ====================

    private long getSmartExpiryDate(String category) {
        Calendar calendar = Calendar.getInstance();
        switch (category) {
            case "Vegetables":
                calendar.add(Calendar.DAY_OF_YEAR, 5);
                break;
            case "Fruits":
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                break;
            case "Dairy":
                calendar.add(Calendar.DAY_OF_YEAR, 10);
                break;
            case "Meat":
                calendar.add(Calendar.DAY_OF_YEAR, 3);
                break;
            case "Frozen":
                calendar.add(Calendar.DAY_OF_YEAR, 30);
                break;
            case "Snacks":
                calendar.add(Calendar.DAY_OF_YEAR, 60);
                break;
            case "Drinks":
                calendar.add(Calendar.DAY_OF_YEAR, 14);
                break;
            default:
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                break;
        }
        return calendar.getTimeInMillis();
    }
}