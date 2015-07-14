package com.somshubra.ragialnotifier.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.somshubra.ragialnotifier.R;
import com.somshubra.ragialnotifier.database.RagialQueryHelper;

/**
 * Created by Yue on 5/30/2015.
 */
public class RagialQueryDialog extends DialogFragment {

    private RagialQueryHelper ragialHelper;

    public void setRagialHelper(RagialQueryHelper ragialHelper) {
        this.ragialHelper = ragialHelper;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle("Enter name to search");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_ragial_query, null, false);
        final EditText ed = (EditText) v.findViewById(R.id.dialog_ragial_edit);
        builder.setView(v);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = ed.getText().toString().trim();

                if (!TextUtils.isEmpty(query)) {
                    if (query.equals("Fallen Angel Wing [1]")) {
                        query = "Fallen Angel Wing";
                    }

                    if (RagialQueryHelper.isExecutorAvailable())
                        ragialHelper.queryRagial(query);
                    else {
                        ragialHelper.restartExecutor();
                        ragialHelper.queryRagial(query);
                    }
                }
            }
        });

        return builder.create();
    }

}
