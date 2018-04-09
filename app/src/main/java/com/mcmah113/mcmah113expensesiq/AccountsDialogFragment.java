package com.mcmah113.mcmah113expensesiq;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;

public class AccountsDialogFragment extends DialogFragment {
    public interface OnCompleteListener {
        void onCompleteLaunchFragment(Bundle callbackData);
    }

    public AccountsDialogFragment() {

    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int accountId = getArguments().getInt("accountId");
        final String callFrom = getArguments().getString("callFrom");

        final AlertDialog.Builder accountDialog = new AlertDialog.Builder(getActivity());

        //getting the layout from the fragment
        final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.fragment_dialog_accounts, null);

        //setting the images from the assets folder for each option
        final AssetManager assetManager = view.getContext().getAssets();
        InputStream inputStream;

        final ImageView imageView1 = view.findViewById(R.id.imageViewTransactions);
        final ImageView imageView2 = view.findViewById(R.id.imageViewMakeATransfer);
        final ImageView imageView3 = view.findViewById(R.id.imageViewAddTransaction);
        final ImageView imageView4 = view.findViewById(R.id.imageViewEditAccount);
        final ImageView imageView5 = view.findViewById(R.id.imageViewDeleteAccount);
        final ImageView imageView6 = view.findViewById(R.id.imageViewHideAccount);

        try {
            inputStream = assetManager.open("account_view.png");
            imageView1.setImageBitmap(BitmapFactory.decodeStream(inputStream));

            inputStream = assetManager.open("account_send.png");
            imageView2.setImageBitmap(BitmapFactory.decodeStream(inputStream));

            inputStream = assetManager.open("account_add_transaction.png");
            imageView3.setImageBitmap(BitmapFactory.decodeStream(inputStream));

            inputStream = assetManager.open("account_transfer.png");
            imageView4.setImageBitmap(BitmapFactory.decodeStream(inputStream));

            inputStream = assetManager.open("account_delete.png");
            imageView5.setImageBitmap(BitmapFactory.decodeStream(inputStream));

            inputStream = assetManager.open("account_hide.png");
            imageView6.setImageBitmap(BitmapFactory.decodeStream(inputStream));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        final LinearLayout buttonViewTransactions = view.findViewById(R.id.ViewTransactions);
        buttonViewTransactions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("View Transactions", accountId, callFrom);
            }
        });

        final LinearLayout buttonMakeATransaction = view.findViewById(R.id.MakeTransfer);
        buttonMakeATransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Make a Transfer", accountId, callFrom);
            }
        });

        final LinearLayout buttonAddTransaction = view.findViewById(R.id.AddTransaction);
        buttonAddTransaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Add Transaction", accountId, callFrom);
            }
        });

        final LinearLayout buttonEditAccount = view.findViewById(R.id.EditAccount);
        buttonEditAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Edit Account", accountId, callFrom);
            }
        });

        final LinearLayout buttonDeleteAccount = view.findViewById(R.id.DeleteAccount);
        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Delete Account", accountId, callFrom);
            }
        });

        final LinearLayout buttonHideAccount = view.findViewById(R.id.HideAccount);
        buttonHideAccount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callBack("Hide Account", accountId, callFrom);
            }
        });

        accountDialog.setView(view);
        accountDialog.setTitle("Select Action");

        return accountDialog.create();
    }

    private void callBack(String selection, int accountId, String callFrom) {
        final Bundle args = new Bundle();
        args.putInt("accountId", accountId);
        args.putString("fragment", selection);
        args.putString("callFrom", callFrom);

        //call back to activity with option selection
        //the activity will close the dialog through the object.dismiss
        //can't close / dismiss the dialog from inside the class
        final OnCompleteListener onCompleteListener = (OnCompleteListener) getActivity();
        onCompleteListener.onCompleteLaunchFragment(args);
    }
}
