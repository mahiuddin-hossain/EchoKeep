package com.suitexen.echokeep.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.suitexen.echokeep.R;

import java.util.ArrayList;

public class StatsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Demo Data for Cards
        setupSummaryCards(view);

        // Setup Chart
        BarChart barChart = view.findViewById(R.id.barChartHistory);
        setupBarChart(barChart);

        return view;
    }

    private void setupSummaryCards(View v) {
        // Find views within the layouts and set data
        ((TextView) v.findViewById(R.id.tvFoodValue)).setText("12.4 kg");
        ((TextView) v.findViewById(R.id.tvFoodLabel)).setText("Food Saved");

        ((TextView) v.findViewById(R.id.tvMoneyValue)).setText("$64.20");
        ((TextView) v.findViewById(R.id.tvMoneyLabel)).setText("Money Saved");
        ((ImageView) v.findViewById(R.id.ivMoneyIcon)).setImageResource(R.drawable.ic_money);

        ((TextView) v.findViewById(R.id.tvCO2Value)).setText("28.5 kg");
        ((TextView) v.findViewById(R.id.tvCO2Label)).setText("CO2 Reduced");
        ((ImageView) v.findViewById(R.id.ivCO2Icon)).setImageResource(R.drawable.ic_eco);
    }

    private void setupBarChart(BarChart barChart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 40));
        entries.add(new BarEntry(2, 60));
        entries.add(new BarEntry(3, 30));
        entries.add(new BarEntry(4, 80));
        entries.add(new BarEntry(5, 50));
        entries.add(new BarEntry(6, 70));

        BarDataSet dataSet = new BarDataSet(entries, "Impact");
        dataSet.setColor(Color.parseColor("#23bc4a")); // Primary Green
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}