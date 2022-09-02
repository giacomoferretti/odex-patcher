package com.giacomoferretti.odexpatcher.example.java.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.giacomoferretti.odexpatcher.library.Art;
import com.giacomoferretti.odexpatcher.library.InstructionSet;

public class MainActivity extends Activity {
    private int padding8;
    private int padding16;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        padding8 = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        padding16 = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));

        ScrollView scrollView = new ScrollView(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // General
        addTitle(linearLayout, "GENERAL");
        addEntry(linearLayout, "InstructionSet.getDefault()", InstructionSet.getDefault().getValue());
        addEntry(linearLayout, "Art.isRuntimeArt()", String.valueOf(Art.isRuntimeArt()));

        // App specific values
        addSeparator(linearLayout);
        addTitle(linearLayout, "APP SPECIFIC");
        addAppSpecific(linearLayout, getPackageName());

        // Native lib test
        if (isPackageInstalled("com.giacomoferretti.odexpatcher.example.nativelib.normal")) {
            addSeparator(linearLayout);
            addTitle(linearLayout, "NATIVE TEST");
            addAppSpecific(linearLayout, "com.giacomoferretti.odexpatcher.example.nativelib.normal");
        }

        scrollView.addView(linearLayout);
        setContentView(scrollView);
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        return true;
    }

    private void addAppSpecific(ViewGroup parent, String packageName) {
        InstructionSet appAbi = InstructionSet.fromPackageName(getPackageManager(), packageName);

        addEntry(parent, "InstructionSet.fromPackageName()", appAbi.getValue());
        try {
            addEntry(parent, "Art.getOatFolder()", Art.getOatFolder(getSourceDir(), appAbi.getValue()));
            addEntry(parent, "Art.getOatFile()", Art.getOatFile(getSourceDir(), appAbi.getValue()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addEntry(parent, "Art.getVdexFile()", Art.getVdexFile(getSourceDir(), appAbi.getValue()));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getSourceDir() throws PackageManager.NameNotFoundException {
        return getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.sourceDir;
    }

    private void addTitle(ViewGroup parent, String title) {
        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setPadding(padding16, padding16, padding16, 0);
        parent.addView(textView);
    }

    private void addEntry(ViewGroup parent, String title, String content) {
        TextView titleTextView = new TextView(this);
        titleTextView.setText(title);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setTypeface(titleTextView.getTypeface(), Typeface.BOLD);
        titleTextView.setPadding(padding16, padding16, padding16, padding8);
        parent.addView(titleTextView);

        TextView contentTextView = new TextView(this);
        contentTextView.setText(content);
        contentTextView.setTextColor(Color.BLACK);
        contentTextView.setPadding(padding16, 0, padding16, padding16);
        parent.addView(contentTextView);
    }

    private void addSeparator(ViewGroup parent) {
        View separator = new View(this);
        separator.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        1,
                        getResources().getDisplayMetrics()
                )
        );
        layoutParams.setMargins(
                padding16,
                padding8,
                padding16,
                padding8
        );
        separator.setLayoutParams(layoutParams);
        parent.addView(separator);
    }
}
