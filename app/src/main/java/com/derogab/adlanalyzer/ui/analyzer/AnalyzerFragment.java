package com.derogab.adlanalyzer.ui.analyzer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.derogab.adlanalyzer.databinding.FragmentAnalyzerBinding;

public class AnalyzerFragment extends Fragment {

    private FragmentAnalyzerBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // View Binding
        binding = FragmentAnalyzerBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }
}
