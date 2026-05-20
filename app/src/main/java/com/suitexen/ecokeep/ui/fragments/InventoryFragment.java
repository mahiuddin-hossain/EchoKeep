package com.suitexen.ecokeep.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.adapters.InventoryAdapter;
import com.suitexen.ecokeep.database.AppDatabase;
import com.suitexen.ecokeep.models.FoodItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InventoryFragment extends Fragment {

    private FloatingActionButton fabAdd;
    private RecyclerView rvInventory;
    private InventoryAdapter adapter;
    private List<FoodItem> inventoryList = new ArrayList<>();
    private Calendar expiryCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    
    private String selectedFilterCategory = "All";
    private String currentSearchQuery = "";
    
    private String selectedImageUri = "";
    private ImageView ivBottomSheetPreview;
    private LinearLayout btnAddPhoto;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri.toString();
                    if (ivBottomSheetPreview != null) {
                        ivBottomSheetPreview.setVisibility(View.VISIBLE);
                        btnAddPhoto.setVisibility(View.GONE);
                        Glide.with(requireContext()).load(uri).into(ivBottomSheetPreview);
                    }
                }
            });

    public InventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabAdd = view.findViewById(R.id.fabAdd);
        rvInventory = view.findViewById(R.id.rvInventory);
        EditText etSearch = view.findViewById(R.id.etSearch);
        ChipGroup cgFilterCategory = view.findViewById(R.id.cgFilterCategory);

        setupRecyclerView();

        // Search Logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                loadInventoryData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Logic
        cgFilterCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    selectedFilterCategory = chip.getText().toString();
                    loadInventoryData();
                }
            } else {
                selectedFilterCategory = "All";
                loadInventoryData();
            }
        });

        fabAdd.setOnClickListener(v -> showAddFoodBottomSheet());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInventoryData();
    }

    private void setupRecyclerView() {
        rvInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        loadInventoryData();
    }

    private void loadInventoryData() {
        List<FoodItem> allItems = AppDatabase.getInstance(requireContext()).foodDao().getAllFoodItems();
        inventoryList.clear();

        for (FoodItem item : allItems) {
            boolean matchesSearch = item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            boolean matchesCategory = selectedFilterCategory.equals("All") || item.getCategory().equalsIgnoreCase(selectedFilterCategory);

            if (matchesSearch && matchesCategory) {
                inventoryList.add(item);
            }
        }

        if (adapter == null) {
            adapter = new InventoryAdapter(inventoryList);
            rvInventory.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void showAddFoodBottomSheet() {
        selectedImageUri = ""; // Reset image selection
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_add_inventory_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        EditText etFoodName = sheetView.findViewById(R.id.etFoodName);
        ChipGroup cgCategory = sheetView.findViewById(R.id.cgCategory);
        EditText etQuantity = sheetView.findViewById(R.id.etQuantity);
        EditText etPrice = sheetView.findViewById(R.id.etPrice);
        Spinner spinnerUnit = sheetView.findViewById(R.id.spinnerUnit);
        LinearLayout btnDatePicker = sheetView.findViewById(R.id.btnDatePicker);
        TextView tvExpiryDate = sheetView.findViewById(R.id.tvExpiryDate);
        SwitchMaterial switchReminder = sheetView.findViewById(R.id.switchReminder);
        LinearLayout layoutAdvanced = sheetView.findViewById(R.id.layoutAdvanced);
        btnAddPhoto = sheetView.findViewById(R.id.btnAddPhoto);
        ivBottomSheetPreview = sheetView.findViewById(R.id.ivFoodPreview);
        MaterialButton btnSave = sheetView.findViewById(R.id.btnSave);

        // Setup Spinner
        String[] units = {"pcs", "kg", "g", "L", "ml", "pack"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, units);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(spinnerAdapter);

        // Image Picker
        btnAddPhoto.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        ivBottomSheetPreview.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        // Date Picker
        btnDatePicker.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                expiryCalendar.set(Calendar.YEAR, year);
                expiryCalendar.set(Calendar.MONTH, month);
                expiryCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvExpiryDate.setText(dateFormat.format(expiryCalendar.getTime()));
            }, expiryCalendar.get(Calendar.YEAR), expiryCalendar.get(Calendar.MONTH), expiryCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Save Button
        btnSave.setOnClickListener(v -> {
            String name = etFoodName.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            EditText etNotes = sheetView.findViewById(R.id.etNotes);
            String notes = etNotes.getText().toString().trim();
            
            int checkedChipId = cgCategory.getCheckedChipId();
            String category = "";
            if (checkedChipId != View.NO_ID) {
                Chip chip = sheetView.findViewById(checkedChipId);
                category = chip.getText().toString();
            }

            if (name.isEmpty() || quantityStr.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill primary fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double quantity = Double.parseDouble(quantityStr);
            double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
            String unit = spinnerUnit.getSelectedItem().toString();
            long expiryDate = expiryCalendar.getTimeInMillis();
            boolean reminder = switchReminder.isChecked();

            // Create FoodItem object
            FoodItem newItem = new FoodItem(name, category, quantity, unit, expiryDate, selectedImageUri, notes, reminder, price, System.currentTimeMillis());

            // Save to Database (Room)
            AppDatabase.getInstance(requireContext()).foodDao().insert(newItem);

            // Update List
            loadInventoryData();
            rvInventory.scrollToPosition(0);

            Toast.makeText(getContext(), "Item Added Successfully", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
