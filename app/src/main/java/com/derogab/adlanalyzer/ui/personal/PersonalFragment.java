package com.derogab.adlanalyzer.ui.personal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.derogab.adlanalyzer.R;
import com.derogab.adlanalyzer.databinding.FragmentPersonalBinding;
import com.derogab.adlanalyzer.models.FormElement;
import com.derogab.adlanalyzer.models.FormGroup;
import com.derogab.adlanalyzer.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class PersonalFragment extends Fragment {

    private static final String TAG = "PersonalFragment";

    private FragmentPersonalBinding binding;

    private PersonalViewModel personalViewModel;
    private List<FormElement> elements;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create ViewModel
        personalViewModel = new ViewModelProvider(requireActivity()).get(PersonalViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // View Binding
        binding = FragmentPersonalBinding.inflate(getLayoutInflater());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(Constants.PERSONAL_DATA_INFORMATION_FILE_NAME, Context.MODE_PRIVATE);

        // Get elements for form
        personalViewModel.getFormTemplate().observe(getViewLifecycleOwner(), new Observer<List<FormGroup>>() {
            @Override
            public void onChanged(List<FormGroup> groups) {

                binding.loader.setVisibility(View.GONE);
                binding.fragmentPersonalSaveButton.show();
                binding.personalFormContent.generate(groups, sharedPreferences, personalViewModel);

            }

        });

        // Saving button to save info
        binding.fragmentPersonalSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.personalFormContent.save(personalViewModel.getFormTemplate().getValue(), sharedPreferences);
                Snackbar.make(v, R.string.personal_fragment_saved, Snackbar.LENGTH_SHORT).show();

            }
        });

    }

}
