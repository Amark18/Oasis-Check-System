package com.akapps.check_verification_system.bottomsheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.check_verification_system.R;
import com.akapps.check_verification_system.classes.VerificationHistory;
import com.akapps.check_verification_system.recyclerview.history_verification_recyclerview;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class VerificationHistorySheet extends RoundedBottomSheetDialogFragment{

    private ArrayList<VerificationHistory> history;

    public VerificationHistorySheet(){}

    public VerificationHistorySheet(ArrayList<VerificationHistory> history){
        this.history = history;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_verification_history, container, false);
        view.setBackgroundColor(requireContext().getColor(R.color.grayDark));

        ImageView closeFilter = view.findViewById(R.id.close_filter);
        RecyclerView historyRecyclerview = view.findViewById(R.id.history_recyclerview);

        // if orientation changes, this prevents app from crashing by closing this bottom sheet
        if(history == null)
            this.dismiss();

        // settings up recyclerview and populate it
        historyRecyclerview.setHasFixedSize(true);
        historyRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        historyRecyclerview.setAdapter(new history_verification_recyclerview(history));

        closeFilter.setOnClickListener(v -> {
            this.dismiss();
        });

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.BaseBottomSheetDialog;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    BottomSheetDialog dialog =(BottomSheetDialog) getDialog ();
                    if (dialog != null) {
                        FrameLayout bottomSheet = dialog.findViewById (R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior behavior = BottomSheetBehavior.from (bottomSheet);
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                });
    }

}